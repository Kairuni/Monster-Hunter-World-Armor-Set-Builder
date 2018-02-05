package mhw_asb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmorSet {
	List<Integer> myArmorIDs;
	String myEquipmentLine;
	String mySkillLine;
	
	ArmorSet(List<Integer> theArmorIDs, String theEquipmentLine) {
		myEquipmentLine = theEquipmentLine;
		mySkillLine = "";
		
		myArmorIDs = new ArrayList<>();
		
		for (int entry : theArmorIDs)
			myArmorIDs.add(entry);
	}
	
	public List<Integer> getMyArmorIDs() {
		List<Integer> retList = new ArrayList<>();

		for (int entry : myArmorIDs)
			retList.add(entry);
		
		return retList;
	}

	public String getEquipmentLine() {
		return myEquipmentLine;
	}

	
	public void buildSkillLine(Statement stmt) throws SQLException {
		StringBuilder skillBuilder = new StringBuilder();
		
		Map<String, Integer> skillToLevel = new HashMap<>();
		for (int entry : myArmorIDs) {
			String skillQuery = "select S.Name, ES.Level from EquipmentSkills as ES inner join Skills as S on S.ID == ES.SID where ES.EID == " + entry;
			ResultSet skillRes = stmt.executeQuery(skillQuery);
			while (skillRes.next()) {
				String name = skillRes.getString("Name");
				int level = skillRes.getInt("Level");
				if (skillToLevel.get(name) != null)
					skillToLevel.put(name, skillToLevel.get(name) + level);
				else
					skillToLevel.put(name, level);
			}
		}
		skillBuilder.append("Total Skills: ");
		boolean first = true;
		for (String key : skillToLevel.keySet()) {
			if (!first)
				skillBuilder.append(", ");
			first = false;
			skillBuilder.append(key + " " + skillToLevel.get(key));
		}
		
		
		mySkillLine = skillBuilder.toString();
	}
	
	public String getSkillLine() {
		return mySkillLine;
	}
	
}
