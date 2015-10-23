package edu.upenn.cis455.storage;

import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis455.bean.DocumentRecord;

public class DocumentRecordDA
{

	public static DocumentRecord getDocument(String documentId) // returns null
																// if the
	// entry wasn't
	// found
	{
		DocumentRecord document = null;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, DocumentRecord> userPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class,
							DocumentRecord.class);
			if (userPrimaryIndex != null)
			{
				document = userPrimaryIndex.get(documentId);
			}
		}
		return document;
	}

	public static DocumentRecord putDocument(DocumentRecord document) // returns
																		// null
																		// is
																		// the
	// user did not exist
	// in the DB
	{
		DocumentRecord insertedDocument = null;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, DocumentRecord> userPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class,
							DocumentRecord.class);
			if (userPrimaryIndex != null)
			{
				insertedDocument = userPrimaryIndex.put(document);
			}
		}
		return insertedDocument;
	}

	public static boolean removeDocument(String documentId)
	{
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, DocumentRecord> userPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class,
							DocumentRecord.class);
			if (userPrimaryIndex != null)
			{
				return userPrimaryIndex.delete(documentId);
			}
		}
		return false;
	}
}
