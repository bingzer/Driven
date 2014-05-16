package com.bingzer.android.driven;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.api.ResultImpl;

public class ResultTest extends AndroidTestCase {

    public void test_cotr(){
        ResultImpl<DrivenException> result = new ResultImpl<DrivenException>();
        assertEquals(true, result.isSuccess());
        assertNull(result.getException());

        result = new ResultImpl<DrivenException>(false);
        assertEquals(false, result.isSuccess());
        assertNull(result.getException());

        result = new ResultImpl<DrivenException>(false, new DrivenException("hi"));
        assertEquals(false, result.isSuccess());
        assertNotNull(result.getException());
        assertEquals("hi", result.getException().getMessage());
    }

    public void test_setSuccess(){
        ResultImpl<DrivenException> result = new ResultImpl<DrivenException>();
        assertEquals(true, result.isSuccess());
        assertNull(result.getException());

        result.setSuccess(true);
        assertEquals(true, result.isSuccess());

        result.setSuccess(false);
        assertEquals(false, result.isSuccess());
    }


    public void test_setException(){
        ResultImpl<DrivenException> result = new ResultImpl<DrivenException>();
        assertEquals(true, result.isSuccess());
        assertNull(result.getException());

        result.setException(new DrivenException("hi"));
        assertEquals(true, result.isSuccess());
        assertNotNull(result.getException());
        assertEquals("hi", result.getException().getMessage());

        result.setException(new DrivenException("hix"));
        assertEquals(true, result.isSuccess());
        assertNotNull(result.getException());
        assertEquals("hix", result.getException().getMessage());

        result.setException(null);
        assertEquals(true, result.isSuccess());
        assertNull(result.getException());
    }

}
