package org.obd.query.exception;

/**
 * An exception resulting from a shard implementation trying to execute
 * an API call. In general the implementation details are hidden from the
 * client
 * @author cjm
 *
 */
public class ShardExecutionException extends Exception {

	public ShardExecutionException() {
		super();
	}

	public ShardExecutionException(String message, Throwable cause) {
		super(message, cause);
		}

	public ShardExecutionException(String message) {
		super(message);
	}

	public ShardExecutionException(Throwable cause) {
		super(cause);
		}

}
