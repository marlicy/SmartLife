package com.service;

public interface AiBusinessToolService {

    String summarizePlatform();

    String lookupVoucher(Long voucherId);

    String createReservation(String sessionId, String shopName, String timeSlot, String note);
}
