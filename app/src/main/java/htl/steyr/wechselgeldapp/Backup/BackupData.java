package htl.steyr.wechselgeldapp.Backup;

import java.util.Date;
import java.util.List;

public class BackupData {
    private List<Transaction> transactions;  // Korrigiert: Generic Type hinzugefügt
    private UserData userData;
    private Date backupDate;
    private String appVersion;
    private int dataVersion;

    public BackupData() {
        this.dataVersion = 1;
        this.appVersion = "1.0";
    }

    public BackupData(List<Transaction> transactions, UserData userData, Date backupDate) {
        this();
        this.transactions = transactions;
        this.userData = userData;
        this.backupDate = backupDate;
    }

    // Getter und Setter
    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public UserData getUserData() {
        return userData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    public Date getBackupDate() {
        return backupDate;
    }

    public void setBackupDate(Date backupDate) {
        this.backupDate = backupDate;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
    }
}
