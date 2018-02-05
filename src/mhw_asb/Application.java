/*
 * Utilizing sqlite-jdbc from https://bitbucket.org/xerial/sqlite-jdbc/wiki/Home
 * Full license text under Xerial_JDBC_License.txt
 * Not including their binaries in this distribution.
 */

package mhw_asb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.ArrayList;

public class Application {
	public static String buildQuery(Statement stmt) throws SQLException {
		List<String> skills = new ArrayList<>();
		List<Integer> skillLevels = new ArrayList<>();
		List<Integer> skillIDs = new ArrayList<>();
		
		String skillString = "";
		int slotCount1, slotCount2, slotCount3;
		slotCount1 = slotCount2 = slotCount3 = 0;
		
		Scanner inputScanner = new Scanner(System.in);
		
		String input = null;

		while (input != "") {
			System.out.println("Enter in an armor skill and level (e.g. Attack Boost 1) to search for. Press enter by itself to end input.");
			if (inputScanner.hasNextLine()) {
				input = inputScanner.findInLine("(.+) (\\d+)");
				
				if (input != null) {
					MatchResult result = inputScanner.match();
					
					if (result.groupCount() != 2) {
						System.out.println("You entered something wrong, try again.");
						// Consume the \n
						inputScanner.nextLine();
						continue;
					}
					
					// Test if the skill is valid.
					ResultSet res = stmt.executeQuery("SELECT * FROM Skills WHERE Name is \"" + result.group(1) + "\"");
					if (res.next()) {
						// The skill existed.
						System.out.println("Added " + result.group(1) + " at level " + result.group(2) + " to the search list.");
						skillIDs.add(res.getInt("ID"));
						skills.add(result.group(1));
						skillLevels.add(Integer.parseInt(result.group(2)));
					} else {
						System.out.println("Failed to match the provided skill to anything in the database.");
						// Consume the \n
						inputScanner.nextLine();
						continue;						
					}

					// Consume the \n
					inputScanner.nextLine();
				} else {
					break;
				}
			}
		}
		// Consume the \n
		inputScanner.nextLine();
		
		System.out.println("Enter the number of level 1 slots requested.");
		slotCount1 = inputScanner.nextInt();
		System.out.println("Enter the number of level 2 slots requested.");
		slotCount2 = inputScanner.nextInt();
		System.out.println("Enter the number of level 3 slots requested.");
		slotCount3 = inputScanner.nextInt();
		
		System.out.println("Requiring " + slotCount1 + " level 1 slots, " + slotCount2 + " level 2 slots, " + slotCount3 + " level 3 slots.");
		
		String skillWhereTest = "";
		
		for (int i = 0; i < skills.size(); i++) {
			if (i != 0)
				skillString += ", ";
			skillString += "\"" + skills.get(i) + "\"";
			
			skillWhereTest += "and\n" +
					"(\n" + 
					"    select sum(Level)\n" + 
					"    from EquipmentSkills as ES\n" + 
					"    where \n" + 
					"        (SID = " + skillIDs.get(i) + " and (ES.EID = hws.ID or ES.EID = tws.ID or ES.EID = aws.ID or ES.EID = wws.id or ES.EID = lws.ID))\n" + 
					"    ) BETWEEN " + skillLevels.get(i) + " and (select Max from Skills where Skills.ID = "+ skillIDs.get(i) +")\n";
		}
		
		System.out.println("Building batch.");
		
		stmt.addBatch("drop table if exists hws;");
		stmt.addBatch("drop table if exists tws");
		stmt.addBatch("drop table if exists aws");
		stmt.addBatch("drop table if exists wws");
		stmt.addBatch("drop table if exists lws");
		stmt.addBatch("drop table if exists tempSkill");
		stmt.addBatch("create temporary table tempSkill as select ID from Skills where Name in (" + skillString + ")");
		
		stmt.addBatch("create temporary table hws as select * from HelmSkillView where SID in tempSkill");
		stmt.addBatch("insert into hws values (0, \"ANY\", 0, 0, 0, 0, 0)");
		stmt.addBatch("create temporary table tws as select * from TorsoSkillView where SID in tempSkill");
		stmt.addBatch("insert into tws values (0, \"ANY\", 0, 0, 0, 0, 0)");
		stmt.addBatch("create temporary table aws as select * from ArmSkillView where SID in tempSkill");
		stmt.addBatch("insert into aws values (0, \"ANY\", 0, 0, 0, 0, 0)");
		stmt.addBatch("create temporary table wws as select * from WaistSkillView where SID in tempSkill");
		stmt.addBatch("insert into wws values (0, \"ANY\", 0, 0, 0, 0, 0)");
		stmt.addBatch("create temporary table lws as select * from LegsSkillView where SID in tempSkill");
		stmt.addBatch("insert into lws values (0, \"ANY\", 0, 0, 0, 0, 0)");
		
		System.out.println("Batch build complete, building query");
		
		String query = "select distinct hws.name as [Helm], tws.name as [Armor], aws.name as [Bracers], wws.name as [Belt], lws.name as [Greaves],\n" + 
				" hws.Slot_1 + tws.Slot_1 + aws.Slot_1 + wws.Slot_1 + lws.Slot_1 as [Level 1 Slot Count],\n" + 
				" hws.Slot_2 + tws.Slot_2 + aws.Slot_2 + wws.Slot_2 + lws.Slot_2 as [Level 2 Slot Count],\n" + 
				" hws.Slot_3 + tws.Slot_3 + aws.Slot_3 + wws.Slot_3 + lws.Slot_3 as [Level 3 Slot Count],\n" + 
				slotCount1 + " as SLOTS_1, " + slotCount2 + " as SLOTS_2, " + slotCount3 + " as SLOTS_3\n" + 
				"from hws, tws, aws, wws, lws\n" + 
				
				"where \n" + 
				"hws.Slot_1 + tws.Slot_1 + aws.Slot_1 + wws.Slot_1 + lws.Slot_1 >= SLOTS_1\n" + 
				"and hws.Slot_2 + tws.Slot_2 + aws.Slot_2 + wws.Slot_2 + lws.Slot_2 >= SLOTS_2\n" + 
				"and hws.Slot_3 + tws.Slot_3 + aws.Slot_3 + wws.Slot_3 + lws.Slot_3 >= SLOTS_3\n";
		
		query += skillWhereTest;		
		
		System.out.println("Query build complete.");
		
		inputScanner.close();
		
		return query;
	}

	public static void main(String[] args) {
		// Load sqlite JDBC driver
		Connection conn = null;
		
		try {
			Class.forName("org.sqlite.JDBC");
			
			
			conn = DriverManager.getConnection("jdbc:sqlite:database.db");
			
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			/*			
			ResultSet res = stmt.executeQuery("SELECT * FROM Equipment WHERE Name like \"Zorah%\"");
			while(res.next()) {
				System.out.println(res.getInt("ID"));
			}*/
			String query = buildQuery(stmt);
			
			stmt.setQueryTimeout(180);

			System.out.println("Executing batch.");
			stmt.executeBatch();
			System.out.println("Complete.");
			
			/*for (int entry : batchRes) {
				System.out.println(entry);
			}*/
			
			System.out.println("\nResults: ");
			
			ResultSet res = stmt.executeQuery(query);
			
			while(res.next()) {
				System.out.println(res.getString("Helm"));
				System.out.println(res.getString("Armor"));
				System.out.println(res.getString("Bracers"));
				System.out.println(res.getString("Belt"));
				System.out.println(res.getString("Greaves"));
				System.out.println();
			}
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {         
	        try {
	              if(conn != null)
	                 conn.close();
	        } catch(SQLException e) {  // Use SQLException class instead.          
	        	System.err.println(e); 
	        }
		}
	}
}
