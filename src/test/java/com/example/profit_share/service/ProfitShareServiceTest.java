package com.example.profit_share.service;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ProfitShareServiceTest {

    enum SessionState {INIT, START, END};

    @Test
    void setMaxClaimableSession() {
        int maxClaimableSession = 0;
        int number = 5;
        maxClaimableSession = number;

        assertTrue(maxClaimableSession == number);
    }

    @Test
    void sessionStart() {
        SessionState sessionState = SessionState.INIT;
        sessionState = SessionState.START;
        List<Float> sessionProfitList = new LinkedList<>();
        sessionProfitList.add(Float.valueOf(0));

        assertTrue(sessionState.equals(SessionState.START));
        assertTrue(sessionProfitList.get(sessionProfitList.size() - 1) == 0);
    }

    @Test
    void sessionStop() {
        SessionState sessionState = SessionState.INIT;
        sessionState = SessionState.END;
        int maxClaimableSession = 5;
        // check sessionProfitArray and upgrade 0 when the session expired.
        List<Float> sessionProfitList = Arrays.asList(new Float[]{1f, 2f, 3f, 4f, 5f, 6f});
        int profitArrayMaxIdx = sessionProfitList.size() - 1;
        if (profitArrayMaxIdx >= maxClaimableSession) {
            int expiredProfitArrayIdx = profitArrayMaxIdx - maxClaimableSession;
            sessionProfitList.set(expiredProfitArrayIdx,  Float.valueOf(0));
        }

        assertTrue(sessionState.equals(SessionState.END));
        assertTrue(sessionProfitList.get(0) == 0);
        assertTrue(sessionProfitList.get(1) == 2);
        assertTrue(sessionProfitList.get(2) == 3);
        assertTrue(sessionProfitList.get(3) == 4);
        assertTrue(sessionProfitList.get(4) == 5);
        assertTrue(sessionProfitList.get(5) == 6);
    }

    @Test
    void addProfit() {
        List<Float> sessionProfitList = Arrays.asList(new Float[]{1f, 2f, 3f, 4f, 5f, 6f});
        float profit = 3;

        int profitArrayMaxIdx = sessionProfitList.size() - 1;
        sessionProfitList.set(profitArrayMaxIdx, Float.sum(sessionProfitList.get(profitArrayMaxIdx), profit));

        assertTrue(sessionProfitList.get(0) == 1);
        assertTrue(sessionProfitList.get(1) == 2);
        assertTrue(sessionProfitList.get(2) == 3);
        assertTrue(sessionProfitList.get(3) == 4);
        assertTrue(sessionProfitList.get(4) == 5);
        assertTrue(sessionProfitList.get(5) == 9);
    }

    @Test
    void getCurrentSession() {
        List<Float> sessionProfitList = Arrays.asList(new Float[]{1f, 2f, 3f, 4f, 5f, 6f});

        assertTrue(sessionProfitList.size() == 6);
    }

    @Test
    void invest() {
        Map<String, Float> investor = new HashMap<>();
        String address = "investor1";
        float amount = 100f;
        float totalInvestment = 100f;

        if (investor.containsKey(address))
            investor.replace(address, Float.sum(investor.get(address), amount));
        else
            investor.putIfAbsent(address, amount);
        totalInvestment = Float.sum(totalInvestment, amount);

        assertTrue(investor.containsKey(address) && address == "investor1");
        assertTrue(investor.get(address) == amount && amount == 100);
        assertTrue(totalInvestment == 200);
    }

    @Test
    void withdraw() {
        Map<String, Float> investor = new HashMap<>();
        investor.put("investor1", 100f);
        String address = "investor1";
        float amount = 100f;
        float totalInvestment = 100f;

        if (investor.containsKey(address))
            investor.replace(address, Float.sum(investor.get(address), -1*amount));
        totalInvestment = Float.sum(totalInvestment, -1*amount);

        assertTrue(investor.get(address) == 0);
        assertTrue(totalInvestment == 0);
    }

    @Test
    void claim() {
        float totalInvestment = 200;
        Map<String, Float> investor = new HashMap<>();
        investor.put("investor1", 100f);
        String address = "investor1";

        // get expiredSessionProfit
        float expiredProfit = 20;

        // claimProfit = expiredProfit * personal investment / total investment
        float claimProfit = expiredProfit * investor.get(address) / totalInvestment;

        assertTrue(claimProfit == 10);
    }

    @Test
    void getExpiredSessionProfit() {
        List<Float> sessionProfitList = Arrays.asList(new Float[]{1f, 2f, 3f, 4f, 5f, 6f});
        int maxClaimableSession = 5;

        float result = 0;
        int profitArrayLength = sessionProfitList.size();
        int profitArrayMinIdx = 0;
        // get sessions in maxClaimableSession
        if (profitArrayLength > maxClaimableSession) {
            profitArrayMinIdx = profitArrayLength - maxClaimableSession;
        }
        for (int i=profitArrayMinIdx; i<=profitArrayLength-1; i++) {
            result = Float.sum(result, sessionProfitList.get(i));
        }

        assertTrue(result == 20);
    }

    @Test
    void reloadSessionProfitArray() {
        List<Float> sessionProfitList = Arrays.asList(new Float[]{1f, 2f, 3f, 4f, 5f, 6f});
        int maxClaimableSession = 5;
        float claimProfit = 20;

        int profitArrayLength = sessionProfitList.size();
        int profitArrayMinIdx = 0;
        // get sessions in maxClaimableSession
        if (profitArrayLength > maxClaimableSession) {
            profitArrayMinIdx = profitArrayLength - maxClaimableSession;
        }
        for (int i=profitArrayMinIdx; i<=profitArrayLength-1; i++) {
            float sessionProfit = sessionProfitList.get(i);
            if (claimProfit >= sessionProfit) {
                sessionProfitList.set(i, Float.valueOf(0));
                claimProfit = Float.sum(claimProfit, -1*sessionProfit);
            } else {
                sessionProfitList.set(i, Float.sum(sessionProfit, -1*claimProfit));
                claimProfit = 0;
            }
        }

        assertTrue(sessionProfitList.get(0) == 1);
        assertTrue(sessionProfitList.get(1) == 0);
        assertTrue(sessionProfitList.get(2) == 0);
        assertTrue(sessionProfitList.get(3) == 0);
        assertTrue(sessionProfitList.get(4) == 0);
        assertTrue(sessionProfitList.get(5) == 0);
    }
}