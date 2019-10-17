module se.uu.ub.cora.indexmessenger {
	requires transitive se.uu.ub.cora.messaging;
	requires transitive se.uu.ub.cora.javaclient;
	requires transitive se.uu.ub.cora.clientdata;

	exports se.uu.ub.cora.indexmessenger;
	exports se.uu.ub.cora.indexmessenger.parser;
}