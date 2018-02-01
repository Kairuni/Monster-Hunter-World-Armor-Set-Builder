select Name, Level
from Skills
inner join EquipmentSkills on EquipmentSkills.SID = Skills.ID
where EID in (
    select EID
    from EquipmentSkills
    inner join Equipment on EquipmentSkills.EID = Equipment.ID
    where Equipment.name = "Zorah Hide Alpha"
    )