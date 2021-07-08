package name.qd.dappe;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalListAccounts;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert.Unit;

public class Dapp {
	private static final String TEST_PKEY = "ee057a93a5327dbca303b745994de5246b36757c646c734e107d973de2908f33";
	private static final String TEST_PKEY2 = "fcf4e9da729d2d36bfb609402902ec33008c2fbbfd1b661338d13278ee3cce1f";
	private static final String CONTRACT_ADDRESS = "0xeCfab06fe2420EB0Dda8dA01093511963e72772c";
	private static final String CONTRACT_ABI = "";
	private static final String TEST_ADDRESS = "0x30aa589Fc4E0e7e8991786374ba9Bea1E70660D5";
	private Web3j web3j;
	private Admin admin;
	private Credentials credentials;

	private Dapp() {
		web3j = Web3j.build(new HttpService());
		admin = Admin.build(new HttpService());
		credentials = getCredentialsFromPrivateKey();
		
		try {
			getVersion();
			getPersonalListAccounts();
			getPersonalAccount();
			getNetVersion();
			getChainId();
			getBalance(TEST_ADDRESS);
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
	}

	private void getVersion() throws IOException, InterruptedException, ExecutionException {
		Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().sendAsync().get();
		String clientVersion = web3ClientVersion.getWeb3ClientVersion();
		System.out.println("version:" + clientVersion);
	}

	private void getPersonalListAccounts() throws InterruptedException, ExecutionException {
		PersonalListAccounts personalListAccounts = admin.personalListAccounts().sendAsync().get();
		System.out.println("personalListAccounts:" + personalListAccounts.getAccountIds());
	}

	private void getPersonalAccount() throws IOException {
		PersonalUnlockAccount personalUnlockAccount = admin
				.personalUnlockAccount("0x30aa589Fc4E0e7e8991786374ba9Bea1E70660D5", "").send();
		System.out.println("getPersonalAccount:" + personalUnlockAccount.accountUnlocked());
	}
	
	private void getNetVersion() throws IOException {
		System.out.println("getNetVersion:" + web3j.netVersion().send().getNetVersion());
	}
	
	private void getChainId() throws IOException {
		System.out.println("getChainId:" + web3j.ethChainId().send().getChainId());
	}

	private Credentials getCredentialsFromPrivateKey() {
		return Credentials.create(TEST_PKEY);
	}
	
	private void getBalance(String address) throws IOException {
		EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
		System.out.println("Balance:" + ethGetBalance.getBalance());
		System.out.println("RawResponse:" + ethGetBalance.getRawResponse());
		System.out.println("Result:" + ethGetBalance.getResult());
	}
	
	private void loadSC() {
		
	}
	
	private void trans() {
		try {
			TransactionReceipt transactionReceipt = Transfer.sendFunds(
			        web3j, credentials, "0x3e694925f348e0887d4a79082facef9f95165fa0",
			        BigDecimal.valueOf(0.11), Unit.ETHER)
			        .send();
			System.out.println(transactionReceipt.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Dapp dapp = new Dapp();
	}
}
