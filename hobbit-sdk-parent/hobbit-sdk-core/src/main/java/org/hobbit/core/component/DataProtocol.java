package org.hobbit.core.component;

import java.nio.ByteBuffer;

public interface DataProtocol {
	void onCommand(ByteBuffer buffer) throws Exception;
	void onData(ByteBuffer buffer) throws Exception;
}
