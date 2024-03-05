package ru.study.entity;

import ru.study.errors.UndoError;
import ru.study.utils.Command;
import ru.study.utils.SnapshotLoad;
import ru.study.utils.SnapshotSave;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

public class Account implements SnapshotSave {
    //fields
    private String nameOwner;
    private HashMap<Currency,Integer> accountBalance;
    private Deque<Command> commands = new ArrayDeque<>();

    //constructors
    //private Account(){}; В unit тесте ошибку компиляции не обработать, поэтому сделал констуктор с выбросом ошибки.
    public Account(){
        throw new IllegalArgumentException("Заполните поле ownerName");
    };
    public Account(String nameOwner){
        accountBalance = new HashMap<>();
        setNameOwner(nameOwner);
    };


    //getters & setters
    public String getNameOwner() {
        return nameOwner;
    }
    public void setNameOwner(String nameOwner) {

        if(nameOwner.equals("")) throw new IllegalArgumentException("Имя не может быть пустым");
        String oldValue = this.nameOwner;
        commands.push(()->this.nameOwner=oldValue);

        this.nameOwner = nameOwner;
    }
    public HashMap<Currency,Integer> getAccountBalance(){
        return new HashMap<>(this.accountBalance);
    }


    //methods
    public void supplementBalance(HashMap<Currency,Integer> balance){
        balance.forEach((k,v)->{
            if (v<0)throw new IllegalArgumentException("Сумма валюты не может быть отрицательной");

            if(this.accountBalance.containsKey(k)){
                Integer oldValue = this.accountBalance.get(k);
                commands.push(()->{
                    this.accountBalance.put(k,oldValue);
                });
            }else {
                commands.push(()->{
                    this.accountBalance.remove(k);
                });
            }

        });
        this.accountBalance.putAll(balance);
    }
    public Account undo(){
        if(commands.isEmpty()) throw new UndoError("Нет действия для отмены");
        commands.pop().perform();
        return this;
    }

    @Override
    public SnapshotLoad save() {
        return new Snapshot();
    }

    @Override
    public String toString() {
        return "Account{" +
                "nameOwner='" + nameOwner + '\'' +
                ", accountBalance=" + accountBalance +
                '}';
    }

    //Snapshot
    private class Snapshot implements SnapshotLoad {
        private String nameOwner;
        private HashMap<Currency,Integer> accountBalance;
        private Deque<Command> commands;
        public Snapshot(){
            this.nameOwner = Account.this.nameOwner;
            this.accountBalance = new HashMap<>(Account.this.accountBalance);
            this.commands = new ArrayDeque<>(Account.this.commands);
        }

        @Override
        public void load() {
            Account.this.nameOwner = this.nameOwner;
            Account.this.accountBalance = new HashMap<>(this.accountBalance);
            Account.this.commands = this.commands;
        }
    }

    //ENUM
    public enum Currency{
        RUB,EURO,DOLLAR,YUAN;
    }


}

