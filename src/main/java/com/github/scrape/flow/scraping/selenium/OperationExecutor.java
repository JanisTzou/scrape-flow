/*
 * Copyright 2021 Janis Tzoumas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.scrape.flow.scraping.selenium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OperationExecutor {

	private static final Logger log = LogManager.getLogger();


	public static <T> T attemptExecute(SupplierOperation<T> operation,
								int tryIntervalMillis,
								int timeoutMillis,
								Logger callerLog,
								String logHead,
								Optional<String> failedSingleTrialLogMsg,
								String completeFailureMsg,
								Optional<String> successLogMsg) throws ProcessingException {


//		log.debug(logHead + " Entering attemptExecute()");

		int cyclesCount = timeoutMillis / tryIntervalMillis;
		int count       = 0;

		while(true) {

			try {
				T t = operation.executeAndGet();
				logSuccess(callerLog, logHead, successLogMsg);
//				log.debug(logHead + " Exiting attemptExecute()");
				return t;
			} catch (Exception exception) {       // WebDriverExceptions we want to retry
				handleException(tryIntervalMillis, callerLog, logHead, failedSingleTrialLogMsg, completeFailureMsg, cyclesCount, count, exception);
			}

			count++;

		} // while

	}


	public static <T> T attemptExecute(SupplierOperation<T> operation,
								OperationPredicate<T> condition,       // defines additional condition for the result
								int tryIntervalMillis,
								int timeoutMillis,
								Logger callerLog,
								String logHead,
								Optional<String> failedSingleTrialLogMsg,
								String completeFailureMsg,
								Optional<String> successLogMsg) throws ProcessingException {


//		log.debug(logHead + " Entering attemptExecute()");

		int cyclesCount = timeoutMillis / tryIntervalMillis;
		int count       = 0;


		while(true) {
			try {
				T t = operation.executeAndGet();
				boolean conditionMet = condition.test(t);

				if (conditionMet) {
					logSuccess(callerLog, logHead, successLogMsg);
//					log.debug(logHead + " Exiting attemptExecute()");
					return t;
				} else if (count > cyclesCount) {
					// if the condition is not met for a long time then throw exception ...
					throw new ProcessingException("Failed to retrieve a value complying with the given condition.");
				} else {
					// we keep trying to meet the condition ...
					logPartialFailure(callerLog, logHead, failedSingleTrialLogMsg, "No values complying with spec. conditions found." );
					sleep(tryIntervalMillis, callerLog);
				}
			} catch (Exception exception) {       // WebDriverExceptions we want to retry
				handleException(tryIntervalMillis, callerLog, logHead, failedSingleTrialLogMsg, completeFailureMsg, cyclesCount, count, exception);
			}

			count++;
		} // while
	}


	public static <T> void attemptExecute(ConsumerOperation operation,
									int tryIntervalMillis,
									int timeoutMillis,
									Logger callerLog,
									String logHead,
									Optional<String> failedSingleTrialLogMsg,
									String completeFailureMsg,
									Optional<String> successLogMsg) throws ProcessingException {


//		log.debug(logHead + " Entering attemptExecute()");

		int cyclesCount = timeoutMillis / tryIntervalMillis;
		int count       = 0;

		while(true) {
			try {
				operation.execute();
				logSuccess(log, logHead, successLogMsg);
				break;
			} catch (Exception exception) {
				handleException(tryIntervalMillis, callerLog, logHead, failedSingleTrialLogMsg, completeFailureMsg, cyclesCount, count, exception);
			}

			count++;
		} // while

//			log.debug(logHead + " Exiting attemptExecute()");
	}


	public static <T> void attemptExecute(ConsumerOperation operation,
									OperationPredicate<T> condition,       // defines additional condition for the result
									int tryIntervalMillis,
									int timeoutMillis,
									Logger callerLog,
									String logHead,
									Optional<String> failedSingleTrialLogMsg,
									String completeFailureMsg,
									Optional<String> successLogMsg) throws ProcessingException {


//		log.debug(logHead + " Entering attemptExecute()");

		int cyclesCount = timeoutMillis / tryIntervalMillis;
		int count       = 0;

		while(true) {
			try {
				operation.execute();
				boolean conditionMet = condition.test(null);

				if (conditionMet) {
					logSuccess(callerLog, logHead, successLogMsg);
//					log.debug(logHead + " Exiting attemptExecute()");
					break;
				} else if (count > cyclesCount) {
					// if the condition is not met for a long time then throw exception ...
					throw new ProcessingException("Failed to retrieve a value complying with the given condition.");
				} else {
					// we keep trying to meet the condition ...
					logPartialFailure(callerLog, logHead, failedSingleTrialLogMsg, "No values complying with spec. conditions found." );
					sleep(tryIntervalMillis, callerLog);
				}
			} catch (Exception exception) {       // WebDriverExceptions we want to retry
				handleException(tryIntervalMillis, callerLog, logHead, failedSingleTrialLogMsg, completeFailureMsg, cyclesCount, count, exception);
			}

			count++;
		} // while

//		log.debug(logHead + " Exiting attemptExecute()");
	}


	// helper
	private static void handleException(int tryIntervalMillis,
			Logger callerLog,
			String logHead,
			Optional<String> failedSingleTrialLogMsg,
			String completeFailureMsg,
			int cyclesCount,
			int count,
			Exception exception) throws ProcessingException {

		if (count == 0) {
			callerLog.error("1st attempt ERROR DETAIL:", exception);
		}
		if (count > cyclesCount) {
			throw new ProcessingException(completeFailureMsg, exception);
		}

		logPartialFailure(callerLog, logHead, failedSingleTrialLogMsg, exception.getClass().getSimpleName());

		sleep(tryIntervalMillis, callerLog);
	}


	// helper
	private static void logSuccess(Logger callerLog, String logHead, Optional<String> successLogMsg) {
		if (successLogMsg.isPresent()) {
//			callerLog.debug(logHead + successLogMsg.get());
		} else {
//			callerLog.debug(logHead + "Operation execution successful.");
		}
	}

	// helper
	private static void logPartialFailure(Logger callerLog, String logHead, Optional<String> failedSingleTrialLogMsg, String causeDescription) {
		if (failedSingleTrialLogMsg.isPresent()) {
			callerLog.warn(logHead + failedSingleTrialLogMsg.get() + " (REASON: " + causeDescription + ")");
		} else {
			callerLog.warn(logHead + "Operation execution failed. Keep trying." + " (REASON: " + causeDescription + ")");
		}
	}

	// helper
	private static void sleep(int tryIntervalMillis, Logger log) {
		try {
			Thread.sleep(tryIntervalMillis);
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

}
