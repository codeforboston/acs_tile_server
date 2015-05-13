import java.nio.charset.Charset;

import org.cfb.jdbf.DBFReader;
import org.cfb.jdbf.JDBFException;


public class testJDBF {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			DBFReader dbfreader = new DBFReader("./src/test/resources/tl_2012_50_tract.dbf");

			int i;
			for (i=0; i<dbfreader.getFieldCount(); i++) {
				System.out.print(dbfreader.getField(i).getName()+"  ");
			}
			System.out.print("\n");
			for(i = 0; dbfreader.hasNextRecord(); i++)
			{
				Object aobj[];

				aobj = dbfreader.nextRecord(Charset.forName("GBK"));

				for (int j=0; j<aobj.length; j++)
					System.out.print(aobj[j]+"  |  ");
				System.out.print("\n");
			}

			System.out.println("Total Count: " + i);

		} catch (JDBFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
