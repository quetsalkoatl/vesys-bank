package bank.server;

import bank.Account;
import bank.InactiveException;
import bank.OverdrawException;
import bank.local.Driver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public enum BankCommand {

    CREATE_ACCOUNT {
        @Override
        String action(String[] arr, Driver.Bank bank) {
            if (arr.length < 2) {
                return "null|Invalid parameters!";
            }
            return bank.createAccount(arr[1]);
        }
    },
    CLOSE_ACCOUNT {
        @Override
        String action(String[] arr, Driver.Bank bank) {
            if (arr.length < 2) {
                return "false";
            }
            return String.valueOf(bank.closeAccount(arr[1]));
        }
    },
    GET_ACCOUNT_NUMBERS {
        @Override
        String action(String[] arr, Driver.Bank bank) {
            Set<String> accounts = bank.getAccountNumbers();
            if (accounts.isEmpty()) {
                return "null";
            }
            return String.join("|", bank.getAccountNumbers());
        }
    },
    GET_ACCOUNT {
        @Override
        String action(String[] arr, Driver.Bank bank) {
            if (arr.length < 2) {
                return "null|Invalid parameters!";
            }
            Account acc = bank.getAccount(arr[1]);
            if (acc == null) {
                return "null";
            } else {
                try {
                    return acc.getNumber()+"|"+acc.getOwner();
                } catch (IOException e) {
                    return "null";
                }
            }
        }
    },
    TRANSFER {
        @Override
        String action(String[] arr, Driver.Bank bank) {
            if (arr.length < 4) {
                return "3|Invalid parameters!";
            }
            Account from = bank.getAccount(arr[1]);
            Account to = bank.getAccount(arr[2]);
            if (from == null || to == null) {
                return "3|Account '" + (from == null ? arr[1] : arr[2]) + "' does not exist!";
            }
            try {
                bank.transfer(from, to, Double.parseDouble(arr[3]));
            } catch (InactiveException e) {
                return "2";
            } catch (OverdrawException e) {
                return "1";
            } catch (Exception e) {
                return "3|" + e.getMessage();
            }
            return "0";
        }
    },
    IS_ACTIVE {
        @Override
        String action(String[] arr, Driver.Bank bank) {
            Account acc = null;
            if (arr.length > 1) {
                acc = bank.getAccount(arr[1]);
            }
            try {
                return String.valueOf(acc != null && acc.isActive());
            } catch (IOException e) {
                return "false";
            }
        }
    },
    DEPOSIT {
        @Override
        String action(String[] arr, Driver.Bank bank) {
            if (arr.length < 3) {
                return "2|Invalid parameters!";
            }
            Account acc = bank.getAccount(arr[1]);
            if (acc == null) {
                return "2|Account '" + arr[1] + "' does not exist!";
            }
            try {
                acc.deposit(Double.parseDouble(arr[2]));
            } catch (InactiveException e) {
                return "1";
            } catch (Exception e) {
                return "2|" + e.getMessage();
            }
            return "0";
        }
    },
    WITHDRAW {
        @Override
        String action(String[] arr, Driver.Bank bank) {
            if (arr.length < 3) {
                return "3|Invalid parameters!";
            }
            Account acc = bank.getAccount(arr[1]);
            if (acc == null) {
                return "2|Account '"+arr[1]+"' does not exist!";
            }
            try {
                acc.withdraw(Double.parseDouble(arr[2]));
            } catch (InactiveException e) {
                return "2";
            } catch (OverdrawException e) {
                return "1";
            } catch (Exception e) {
                return "3|" + e.getMessage();
            }
            return "0";
        }
    },
    GET_BALANCE {
        @Override
        String action(String[] arr, Driver.Bank bank) {
            if (arr.length < 2) {
                return "0";
            }
            Account acc = bank.getAccount(arr[1]);
            if (acc == null) {
                return "0";
            }
            try {
                return String.valueOf(acc.getBalance());
            } catch (IOException e) {
                return "0";
            }
        }
    },
    ERROR {
        @Override
        String action(String[] arr, Driver.Bank bank) {
            return "err";
        }
    };

    static BankCommand fromString(String s) {
        String search = s.replaceAll("_", "");
        return Arrays.stream(BankCommand.values())
                .filter(c -> c.toString().replaceAll("_", "").equalsIgnoreCase(search))
                .findFirst()
                .orElse(ERROR);
    }

    abstract String action(String[] arr, Driver.Bank bank);

}
