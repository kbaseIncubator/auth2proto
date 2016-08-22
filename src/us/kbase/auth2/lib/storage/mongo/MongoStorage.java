package us.kbase.auth2.lib.storage.mongo;

import java.util.Base64;

import com.mongodb.client.MongoDatabase;

import us.kbase.auth2.lib.LocalUser;
import us.kbase.auth2.lib.storage.AuthStorage;

public class MongoStorage implements AuthStorage {

	//TODO TEST unit tests
	//TODO TEST authenticate to db
	//TODO JAVADOC
	
	public MongoStorage(final MongoDatabase db) {
		if (db == null) {
			throw new NullPointerException("db");
		}
		
		//TODO NOW indexes
		//TODO NOW check workspace startup for stuff to port over
		//TODO NOW check / set config with root user
		//TODO NOW check root password or something and warn if not set
		
	}
	
	@Override
	public void createLocalAccount(final LocalUser local) {
		// TODO Auto-generated method stub
		System.out.println("***************createLocalAccount**********");
		System.out.println(local.getUserName());
		System.out.println(Base64.getEncoder().encodeToString(
				local.getPasswordHash()));
		System.out.println(Base64.getEncoder().encodeToString(local.getSalt()));
	}

}
