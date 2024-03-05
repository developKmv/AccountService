
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.study.entity.Account;
import ru.study.errors.UndoError;
import ru.study.utils.SnapshotLoad;

import java.util.HashMap;


@Slf4j
public class AccountServiceTests {

    @Test
    void createAccount(){
       Assertions.assertThrows(IllegalArgumentException.class,()->{
          new Account();
       });
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            new Account("");
        });
        Assertions.assertEquals("test",new Account("test").getNameOwner());
    }
    @Test
    void checkBalance(){
        Account a = new Account("test");
        HashMap<Account.Currency,Integer> balance = new HashMap<>();
        balance.put(Account.Currency.RUB,1000);
        balance.put(Account.Currency.DOLLAR,500);
        balance.put(Account.Currency.EURO,300);
        balance.put(Account.Currency.YUAN,2000);
        balance.put(Account.Currency.RUB,7000);

        Assertions.assertEquals(7000,balance.get(Account.Currency.RUB));

        a.supplementBalance(balance);
        log.info(a.getAccountBalance().toString());

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            balance.put(Account.Currency.DOLLAR,-1000);
            a.supplementBalance(balance);
        });
    }
    @Test
    public void testUndoMethodSetName(){
        Account account = new Account("test1");
        account.setNameOwner("same_test");
        log.info(account.toString());
        account.undo();
        log.info(account.toString());
        Assertions.assertEquals("test1",account.getNameOwner());

    }

    @Test
    public void testUndoMethodError(){
        Account account = new Account("test2");
        account.undo();
        Assertions.assertThrows(UndoError.class,()->{
            account.undo();
        });
    }
    @Test
    public void testUndoMethodBalance(){
        Account account = new Account("test3");
        HashMap<Account.Currency,Integer> newBalance = new HashMap<>();
        newBalance.put(Account.Currency.EURO,100);
        account.supplementBalance(newBalance);
        log.info(account.toString());
        account.undo();
        log.info(account.toString());
        //remove
        Assertions.assertEquals(0,account.getAccountBalance().size());

        //revert
        newBalance.put(Account.Currency.EURO,100);
        account.supplementBalance(newBalance);

        newBalance.put(Account.Currency.RUB,100);
        newBalance.put(Account.Currency.EURO,200);
        account.supplementBalance(newBalance);
        log.info(account.toString());
        account.undo();
        log.info(account.toString());
        account.undo();
        log.info(account.toString());
        Assertions.assertEquals(100,account.getAccountBalance().get(Account.Currency.EURO));
    }

    @Test
    public void SnapshotTest(){
        Account account = new Account("snapshot");
        HashMap<Account.Currency,Integer> newBalance = new HashMap<>();
        newBalance.put(Account.Currency.EURO,100);
        account.supplementBalance(newBalance);
        log.info(account.toString());

        SnapshotLoad save1 = account.save();

        newBalance.put(Account.Currency.YUAN,600);
        account.supplementBalance(newBalance);
        account.setNameOwner("anyName");
        log.info(account.toString());

        save1.load();
        log.info("save1 load: " + account.toString());

        //
        Account accountAs = new Account("snapshot");
        HashMap<Account.Currency,Integer> newBalanceAs = new HashMap<>();
        newBalanceAs.put(Account.Currency.EURO,100);
        accountAs.supplementBalance(newBalanceAs);
        Assertions.assertEquals(accountAs.getNameOwner(),account.getNameOwner());
        Assertions.assertEquals(accountAs.getAccountBalance(),account.getAccountBalance());


        newBalance.put(Account.Currency.DOLLAR,100);
        account.supplementBalance(newBalance);
        newBalance.put(Account.Currency.RUB,700);
        account.supplementBalance(newBalance);
        account.setNameOwner("ggggg");

        SnapshotLoad save2 = account.save();
        save1.load();
        log.info("save1 load: " + account.toString());
        Assertions.assertEquals(accountAs.getNameOwner(),account.getNameOwner());
        Assertions.assertEquals(accountAs.getAccountBalance(),account.getAccountBalance());
        save2.load();
        log.info("save2 load: " + account.toString());

    }

}
