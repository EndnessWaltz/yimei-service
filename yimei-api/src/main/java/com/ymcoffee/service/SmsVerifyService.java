package com.ymcoffee.service;

import com.ymcoffee.base.exception.ExceptionCode;
import com.ymcoffee.base.exception.ServiceException;
import com.ymcoffee.base.tools.CommonUtils;
import com.ymcoffee.dao.hibernate.SmsVerifyRepository;
import com.ymcoffee.model.SmsVerify;
import com.ymcoffee.util.RedisUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;


@Service
public class SmsVerifyService {
    @Autowired
    private SmsService smsService;


    @Value("${buisness.smsCodeValidSeconds}")
    private Integer smsCodeValidSeconds;

    @Autowired
    private SmsVerifyRepository smsVerifyRepository;

    /**
     * 发送验证码
     *
     * @param mobile
     */
    public String sendSmsVerify(String mobile) {
        Jedis jedis = RedisUtils.getJedisPoolInstance().getResource();
        if (jedis.exists(mobile)) {
            throw new ServiceException(ExceptionCode.PARAM_TYPE_ERROR, "验证码在60秒内请求过多");
        };
        String verifyCode = CommonUtils.genRandomNumCode(4);
        smsService.sendSms("86", mobile, verifyCode);

        SmsVerify smsVerify = new SmsVerify();
        smsVerify.setCode(verifyCode);
        smsVerify.setMobile(mobile);
        smsVerify.setValid(true);
        smsVerifyRepository.save(smsVerify);
        jedis.set(mobile, "1", "NX", "EX", 59);
        return verifyCode;
    }

    /**
     * 验证 验证码
     *
     * @param mobile
     * @param code
     * @return
     */
    public void validSmsVerify(String mobile, String code) {
        List<SmsVerify> smsVerifies = smsVerifyRepository.findValid(mobile, code, smsCodeValidSeconds);
        if (CollectionUtils.isEmpty(smsVerifies)) {
            throw new ServiceException(ExceptionCode.INVALID_CODE, "验证码无效");
        }
        smsVerifies.forEach(each -> {
            each.setValid(false);
            smsVerifyRepository.merge(each);
        });
    }
}
