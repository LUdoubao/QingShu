package org.doubao.payment.service.service;

import org.doubao.payment.service.dto.PaymentRecordDto;

public interface PaymentService {
	void pay(PaymentRecordDto paymentRecord);
}