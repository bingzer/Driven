package com.dropbox.client2;

public class MockAccount extends DropboxAPI.Account {

    public MockAccount(String country, String displayName, String email, boolean emailVerified, long uid, String referralLink, boolean isPaired, String locale, NameDetails nameInfo, TeamInfo teamInfo, long quota, long quotaNormal, long quotaShared) {
        super(country, displayName, email, emailVerified, uid, referralLink, isPaired, locale, nameInfo, teamInfo, quota, quotaNormal, quotaShared);
    }


    public static class NameDetails extends DropboxAPI.NameDetails {
        public NameDetails(String givenName, String surname, String familiarName) {
            super(givenName, surname, familiarName);
        }
    }

    public static class TeamInfo extends DropboxAPI.TeamInfo {
        public TeamInfo(String teamId, String name) {
            super(teamId, name);
        }
    }
}
