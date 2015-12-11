package edu.upenn.cis455.project.storage;

import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis455.project.bean.DocumentRecord;

// TODO: Auto-generated Javadoc
/**
 * Data Accessor class for the document entity class.
 *
 * @author cis455
 */
public class DocumentRecordDA
{

	/**
	 * Gets the document.
	 *
	 * @param documentId
	 *            the document id
	 * @return the document
	 */
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

	/**
	 * Put document.
	 *
	 * @param document
	 *            the document
	 * @return the document record
	 */
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

	/**
	 * Removes the document.
	 *
	 * @param documentId
	 *            the document id
	 * @return true, if successful
	 */
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

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public static long getSize()
	{
		long result = -1;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, DocumentRecord> userPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class,
							DocumentRecord.class);
			if (userPrimaryIndex != null)
			{
				result = userPrimaryIndex.count();
			}
		}
		return result;
	}
}
