package org.dominokit.domino.rest;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

public class NestedRequestBean {

    @PathParam(value = "accountNumber")
    private int account;
    @QueryParam("accountAlias")
    private String accountName;
    @HeaderParam("ownerAddress")
    public String address;

    public int getAccount() {
        return account;
    }

    public void setAccount(int account) {
        this.account = account;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
