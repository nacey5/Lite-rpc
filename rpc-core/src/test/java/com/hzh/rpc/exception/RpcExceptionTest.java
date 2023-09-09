package com.hzh.rpc.exception;

import com.hzh.rpc.exception.errorcode.ErrorCode;
import com.hzh.rpc.exception.errorcode.SystemErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class RpcExceptionTest {

    @Mock
    private ErrorCode mockErrorCode;

    private RpcException rpcExceptionUnderTest;

    @Before
    public void setUp() {
        rpcExceptionUnderTest = new RpcException(SystemErrorCode.ILLEGAL_ARGUMENT_ERROR, new IllegalArgumentException(), "mock illegal exception");
    }


    @Test(expected = RpcException.class)
    public void testThrow() {
        thrSome();
    }

    @Test
    public void testTryCatch() {
        try {
            throw rpcExceptionUnderTest;
        } catch (RpcException r) {
            doSome();
        }
    }

    @Test
    public void testGetExceptionMsg(){
        try {
            throw rpcExceptionUnderTest;
        } catch (RpcException r) {
            assertEquals(SystemErrorCode.ILLEGAL_ARGUMENT_ERROR.getMessage(),r.getMessage());
        }
    }


    private void doSome() {
        log.info("doSome...");
    }

    private void thrSome() throws RpcException {
        throw rpcExceptionUnderTest;
    }


}
