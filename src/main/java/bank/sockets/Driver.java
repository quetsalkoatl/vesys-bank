package bank.sockets;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import bank.InactiveException;
import bank.OverdrawException;

public class Driver implements bank.BankDriver {

	private final Socket s = new Socket();
	private DataInputStream is;
	private DataOutputStream os;

	@Override
	public void connect(String[] args) throws IOException {
		this.s.connect(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
		this.is = new DataInputStream(s.getInputStream());
		this.os = new DataOutputStream(s.getOutputStream());
	}

	@Override
	public void disconnect() throws IOException {
		is.close();
		os.close();
		s.close();
	}

	@Override
	public bank.Bank getBank() {
		return new Bank();
	}

	public class Bank implements bank.Bank {

		@Override
		public String createAccount(String owner) throws IOException {
			os.writeUTF("createAccount|"+owner);
//			System.out.println("Called: createAccount");
			return is.readUTF();
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			os.writeUTF("closeAccount|"+number);
//			System.out.println("Called: closeAccount");
			return Boolean.parseBoolean(is.readUTF());
		}

		@Override
		public Set<String> getAccountNumbers() throws IOException {
			os.writeUTF("getAccountNumbers");
//			System.out.println("Called: getAccountNumbers");
			String[] resp = is.readUTF().split("\\|");
			if (resp[0].equals("err")) {
				System.out.println(String.join("|", resp));
				return new HashSet<>();
			}
			return Arrays.stream(resp)
					.filter(nr -> !"null".equals(nr))
					.collect(Collectors.toSet());
		}

		@Override
		public Account getAccount(String s) throws IOException {
			os.writeUTF("getAccount|"+s);
//			System.out.println("Called: getAccount");
			String[] resp = is.readUTF().split("\\|");
			if (resp.length < 2 || resp[0].equals("null")) {
				return null;
			}
			return new Account(resp[0], resp[1]);
		}

		@Override
		public void transfer(bank.Account from, bank.Account to, double amount) throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
			os.writeUTF("transfer|"+from.getNumber()+"|"+to.getNumber()+"|"+amount);
//			System.out.println("Called: transfer");
			String[] resp = is.readUTF().split("\\|");
			switch (resp[0]) {
				case "0": return;
				case "1": throw new OverdrawException();
				case "2": throw new InactiveException();
				default: throw new IllegalArgumentException(resp.length > 1 ? resp[1] : resp[0]);
			}
		}


	}

	public class Account implements bank.Account {

		private String number;
		private String owner;

		public Account(String number, String owner) {
			this.number = number;
			this.owner = owner;
		}

		@Override
		public String getNumber() throws IOException {
			return number;
		}

		@Override
		public String getOwner() throws IOException {
			return owner;
		}

		@Override
		public boolean isActive() throws IOException {
			os.writeUTF("isActive|"+number);
//			System.out.println("Called: isActive("+number+")");
			return Boolean.parseBoolean(is.readUTF());
		}

		@Override
		public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
			os.writeUTF("deposit|"+number+"|"+amount);
//			System.out.println("Called: deposit("+number+")");
			String[] resp = is.readUTF().split("\\|");
			switch (resp[0]) {
				case "0": return;
				case "1": throw new InactiveException();
				default: throw new IllegalArgumentException(resp.length > 1 ? resp[1] : resp[0]);
			}
		}

		@Override
		public void withdraw(double amount) throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
			os.writeUTF("withdraw|"+number+"|"+amount);
//			System.out.println("Called: withdraw("+number+")");
			String[] resp = is.readUTF().split("\\|");
			switch (resp[0]) {
				case "0": return;
				case "1": throw new OverdrawException();
				case "2": throw new InactiveException();
				default: throw new IllegalArgumentException(resp.length > 1 ? resp[1] : resp[0]);
			}
		}

		@Override
		public double getBalance() throws IOException {
			os.writeUTF("getBalance|"+number);
//			System.out.println("Called: getBalance("+number+")");
			return Double.parseDouble(is.readUTF());
		}
	}

}
