package com.example.profit_share.service;

import com.example.profit_share.exception.PsException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProfitShareService {

    private enum SessionState {INIT, START, END};
    private SessionState sessionState = SessionState.INIT;
    private int maxClaimableSession = 0;
    private String owner;
    private Map<String, Float> investor = new HashMap<>();

    private float totalInvestment;
    // record total profit in each session.
    // And the length of sessionProfitList is the count of the session.
    private List<Float> sessionProfitList = new LinkedList<>();


    /** owner */

    public void setMaxClaimableSession(int number, String address) {
        checkOwner(address);
        checkSessionStatusInit();

        maxClaimableSession = number;
        sessionState = SessionState.END;
    }

    public void sessionStart(String address) {
        checkOwner(address);
        checkSessionStatusEnd();

        sessionState = SessionState.START;
        // add a new profit in the beginning of the session.
        sessionProfitList.add(Float.valueOf(0));

        log.info("Session %d start", sessionProfitList.size());
    }

    public void sessionStop(String address) {
        checkOwner(address);
        checkSessionStatusStart();

        sessionState = SessionState.END;
        // check sessionProfitArray and upgrade 0 when the session expired.
        int profitArrayMaxIdx = sessionProfitList.size() - 1;
        if (profitArrayMaxIdx >= maxClaimableSession) {
            int expiredProfitArrayIdx = profitArrayMaxIdx - maxClaimableSession;
            sessionProfitList.set(expiredProfitArrayIdx,  Float.valueOf(0));
        }
        printSessionProfitArray();

        log.info("Session %d stop", sessionProfitList.size());
    }

    public void addProfit (float profit, String address) {
        checkOwner(address);
        checkSessionStatusStart();
        if (profit < 0)
            throw PsException.occur("10005", "profit should > 0");

        int profitArrayMaxIdx = sessionProfitList.size() - 1;
        sessionProfitList.set(profitArrayMaxIdx, Float.sum(sessionProfitList.get(profitArrayMaxIdx), profit));

        log.info("add profit %d success.", profit);
        log.info("sum of the expired session profit is %d.", getExpiredSessionProfit());
    }


    /** everyone */

    public int getCurrentSession() {
        return sessionProfitList.size();
    }


    /** investor */

    public void invest(Float amount, String address) {
        checkSessionStatusStart();
        if (amount < 0) throw PsException.occur("10005", "Amount should > 0");

        if (investor.containsKey(address))
            investor.replace(address, Float.sum(investor.get(address), amount));
        else
            investor.putIfAbsent(address, amount);
        totalInvestment = Float.sum(totalInvestment, amount);

        log.info("add investment %d success.", amount);
        log.info("sum of the investment is %d.", investor.get(address));
        log.info("total investment for all investors is %d.", totalInvestment);
    }

    public void withdraw(Float amount, String address) {
        checkSessionStatusStart();
        if (amount < 0) throw PsException.occur("10005", "Amount should > 0");

        if (investor.containsKey(address)) {
            if (investor.get(address) < amount)
                throw PsException.occur("10007", "Insufficient balance.");
            investor.replace(address, Float.sum(investor.get(address), -1*amount));
        } else
            throw PsException.occur("10006", "This investor doesn't invest yet.");
        totalInvestment = Float.sum(totalInvestment, -1*amount);

        log.info("withdraw investment %d success.", amount);
        log.info("sum of the investment is %d.", investor.get(address));
        log.info("total investment for all investors is %d.", totalInvestment);
    }

    public float claim(String address) {
        checkSessionStatusStart();
        if (!investor.containsKey(address))
            throw PsException.occur("10006", "This investor doesn't invest yet.");

        // get expiredSessionProfit
        float expiredProfit = getExpiredSessionProfit();
        log.info("sum of the expired session profit is %d.", expiredProfit);

        // claimProfit = expiredProfit * personal investment / total investment
        float claimProfit = expiredProfit * investor.get(address) / totalInvestment;
        log.info("get the claim profit is %d.", claimProfit);

        // reload sessionProfitArray
        reloadSessionProfitArray(claimProfit);
        printSessionProfitArray();

        return claimProfit;
    }

    protected float getExpiredSessionProfit() {
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

        return result;
    }

    protected void reloadSessionProfitArray(float claimProfit) {
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
    }

    private void printSessionProfitArray() {
        int profitArrayMaxIdx = sessionProfitList.size() - 1;
        log.info("print sessionProfitArray: ");
        for (int i=0; i<=profitArrayMaxIdx; i++) {
            log.info("[%d] :   %d", i, sessionProfitList.get(i));
        }
    }

    private boolean checkOwner(String address) {
        if (StringUtils.isEmpty(address)) owner = address;
        if (owner.equalsIgnoreCase(address))
            return true;
        throw PsException.occur("10001", "This function would be executed only by owner.");
    }

    private boolean checkSessionStatusInit() {
        if (sessionState.equals(SessionState.INIT))
            return true;
        throw PsException.occur("10002", "This function would be executed only when session status equals init.");
    }

    private boolean checkSessionStatusStart() {
        if (sessionState.equals(SessionState.START))
            return true;
        throw PsException.occur("10003", "This function would be executed only when session status equals start.");
    }

    private boolean checkSessionStatusEnd() {
        if (sessionState.equals(SessionState.END))
            return true;
        throw PsException.occur("10004", "This function would be executed only when session status equals end.");
    }
 }
