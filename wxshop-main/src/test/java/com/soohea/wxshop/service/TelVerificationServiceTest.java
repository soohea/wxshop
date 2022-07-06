package com.soohea.wxshop.service;

import com.soohea.wxshop.controller.AuthController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TelVerificationServiceTest {
    public static AuthController.TelAndCode VALID_PARAMETER = new AuthController.TelAndCode("13800000000", null);
    public static AuthController.TelAndCode VALID_PARAMETER_CODE = new AuthController.TelAndCode("13800000000", "000000");
    public static AuthController.TelAndCode EMPTY_TEL = new AuthController.TelAndCode(null, null);
    public static AuthController.TelAndCode WRONG_CODE = new AuthController.TelAndCode("13800000000", "123456");

    @Test
    public void returnTrueValid() {
        Assertions.assertTrue(new TelVerificationService().verificationParameter(VALID_PARAMETER));
    }

    @Test
    public void returnFalseIfNoTel() {
        Assertions.assertFalse(new TelVerificationService().verificationParameter(EMPTY_TEL));
    }


}
