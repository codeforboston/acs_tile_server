import org.cfb.ungentry.census.data.SequenceAndTableNumber;


public class testSequence {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		SequenceAndTableNumber aSeq = new SequenceAndTableNumber("./src/test/resources/Sequence_Number_and_Table_Number_Lookup.csv");
		aSeq.read();

	}

}
