/*
 * Copyright (c) 2020 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import bank.InactiveException;
import bank.OverdrawException;

public class Driver implements bank.BankDriver {
	private Bank bank = null;

	@Override
	public void connect(String[] args) {
		bank = new Bank();
		System.out.println("connected...");
	}

	@Override
	public void disconnect() {
		bank = null;
		System.out.println("disconnected...");
	}

	@Override
	public bank.Bank getBank() {
		return bank;
	}

	public static class Bank implements bank.Bank {

		private final String ACCOUNT_PREFIX = "123-09-";

		private final Map<String, Account> accounts = new HashMap<>();

		@Override
		public Set<String> getAccountNumbers() {
			return accounts.entrySet().stream()
					.filter(e -> e.getValue().isActive())
					.map(Map.Entry::getKey)
					.collect(Collectors.toSet());
		}

		@Override
		public String createAccount(String owner) {
			int nr = accounts.keySet().stream()
					.mapToInt(value -> Integer.parseInt(value.replace(ACCOUNT_PREFIX, "")))
					.max()
					.orElse(-1) + 1;
			String no = ACCOUNT_PREFIX + String.format("%04d", nr);
			Account a = new Account(no, owner);
			accounts.put(no, a);
			return no;
		}

		@Override
		public boolean closeAccount(String number) {
			Account acc = accounts.get(number);
			if (acc == null || !acc.isActive() || acc.getBalance() > 0) {
				return false;
			}
			acc.active = false;
			return true;
		}

		@Override
		public bank.Account getAccount(String number) {
			return accounts.get(number);
		}

		@Override
		public void transfer(bank.Account from, bank.Account to, double amount)
				throws IOException, InactiveException, OverdrawException {
			from.withdraw(amount);
			try {
				to.deposit(amount);
			} catch (Exception e) {
				from.deposit(amount);
				throw e;
			}
		}

	}

	private static class Account implements bank.Account {
		private String number;
		private String owner;
		private double balance;
		private boolean active = true;

		private Account(String number, String owner) {
			this.owner = owner;
			this.number = number;
		}

		@Override
		public double getBalance() {
			return balance;
		}

		@Override
		public String getOwner() {
			return owner;
		}

		@Override
		public String getNumber() {
			return number;
		}

		@Override
		public boolean isActive() {
			return active;
		}

		@Override
		public void deposit(double amount) throws InactiveException {
			if (!isActive()) {
				throw new InactiveException();
			}
			if (amount < 0) {
				throw new IllegalArgumentException("amount ("+amount+") must be >= 0");
			}
			balance += amount;
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException {
			if (!isActive()) {
				throw new InactiveException();
			}
			if (balance < amount) {
				throw new OverdrawException();
			}
			if (amount < 0) {
				throw new IllegalArgumentException("amount ("+amount+") must be >= 0");
			}
			balance -= amount;
		}

	}

}