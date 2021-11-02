/*
 * Copyright 2019 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.uu.ub.cora.indexmessenger;

import java.util.List;

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.javaclient.cora.CoraClient;

public class CoraClientSpy implements CoraClient {

	public boolean createWasCalled = false;
	public String createdRecordType = "";
	public ClientDataGroup createdDataGroup;
	public boolean throwErrorOnCreate = false;

	@Override
	public String create(String recordType, String json) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String create(String recordType, ClientDataGroup dataGroup) {
		createWasCalled = true;
		createdRecordType = recordType;
		createdDataGroup = dataGroup;
		if (throwErrorOnCreate) {
			throw new RuntimeException("Error from CoraClientSpy on create");
		}
		return null;
	}

	@Override
	public String read(String recordType, String recordId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String update(String recordType, String recordId, String json) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String delete(String recordType, String recordId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String readList(String recordType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String readIncomingLinks(String recordType, String recordId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientDataRecord readAsDataRecord(String recordType, String recordId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String update(String recordType, String recordId, ClientDataGroup dataGroup) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ClientDataRecord> readListAsDataRecords(String recordType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String indexData(ClientDataRecord clientDataRecord) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String indexData(String recordType, String recordId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String removeFromIndex(String recordType, String recordId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String indexDataWithoutExplicitCommit(String recordType, String recordId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String indexRecordsOfType(String recordType, String filterAsJson) {
		// TODO Auto-generated method stub
		return null;
	}

}
