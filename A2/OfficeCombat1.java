import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Simple combat simulator that reads fighters and weapons from standard input.
 */
public class OfficeCombat1 {
	
	// To test if all fields in a class is not public
	/**
	 * Tests whether every declared field of a class is private.
	 *
	 * @param clazz the class to inspect
	 * @return true if all declared fields are private, false otherwise
	 */
	public static boolean areAllFieldsPrivate(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isPrivate(field.getModifiers())) {
                return false;
            }
        }
        return true;
    }
	
	/**
	 * Executes the combat simulation using input from stdin.
	 *
	 * @param args unused program arguments
	 * @throws IOException when reading input fails
	 */
	public static void main(String[] args) throws IOException {
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader inData = new BufferedReader(isr);
			
		// Input combat data
		String c1_info[] = inData.readLine().split(" ");
		String c2_info[] = inData.readLine().split(" ");
		String w1_info[] = inData.readLine().split(" ");
		String w2_info[] = inData.readLine().split(" ");
		
		Character c1 = new Character(c1_info[0], Integer.valueOf(c1_info[2]), Integer.valueOf(c1_info[1]));
		Character c2 = new Character(c2_info[0], Integer.valueOf(c2_info[2]), Integer.valueOf(c2_info[1]));
		Weapon w1 = new Weapon(w1_info[0], Integer.valueOf(w1_info[1]));
		Weapon w2 = new Weapon(w2_info[0], Integer.valueOf(w2_info[1]));
		
		// Start fighting
		System.out.println("Now fighting: " + c1.getName() + " VS " + c2.getName());
		System.out.println("Skill level of " + c1.getName() + ": " + c1.getSkillLevel());
		System.out.println("Skill level of " + c2.getName() + ": " + c2.getSkillLevel());
		System.out.println("Energy level of " + c1.getName() + ": " + c1.getEnergyLevel());
		System.out.println("Energy level of " + c2.getName() + ": " + c2.getEnergyLevel());
		System.out.println("----------------------------");
		
		int round = 0;
		while (!c1.isLose() && !c2.isLose()) {
			if (round % 2 == 0) {
				int attackAmount = c1.attack(w1);
				int hurtAmount = c2.hurt(attackAmount);
				
				System.out.println(c1.getName() + " makes an attack by " + w1.getName() + "!");
				System.out.println(c2.getName() + " takes a hurt of " + hurtAmount + "! Remaining energy becomes " + c2.getEnergyLevel() + ".");
			} 
			else {
				int attackAmount = c2.attack(w2);
				int hurtAmount = c1.hurt(attackAmount);
				
				System.out.println(c2.getName() + " makes an attack by " + w2.getName() + "!");
				System.out.println(c1.getName() + " takes a hurt of " + hurtAmount + "! Remaining energy becomes " + c1.getEnergyLevel() + ".");
			}
			round++;
		}
		
		if (c1.isLose()) {
			System.out.println(c2.getName() + " wins!");
		}
		else {
			System.out.println(c1.getName() + " wins!");
		}
		
		// Test if all class fields are private
		System.out.println("----------------------------");
		System.out.println("Test if all the class fields are private");
		System.out.println("Character: " + areAllFieldsPrivate(Character.class));
		System.out.println("Weapon: " + areAllFieldsPrivate(Weapon.class));
	}

	/**
	 * Combatant with basic attributes and actions for the duel.
	 */
	private static class Character {
		private final String name;
		private final int skillLevel;
		private int energyLevel;
		
		/**
		 * Creates a character with the supplied stats.
		 *
		 * @param name the character name
		 * @param skillLevel the attack skill rating
		 * @param energyLevel the starting energy amount
		 */
		public Character(String name, int skillLevel, int energyLevel) {
			this.name = name;
			this.energyLevel = energyLevel;
			this.skillLevel = skillLevel;
		}
		
		/**
		 * Retrieves the character name.
		 *
		 * @return the name
		 */
		public String getName() {
			return this.name;
		}
		
		/**
		 * Retrieves the skill rating.
		 *
		 * @return the skill level
		 */
		public int getSkillLevel() {
			return this.skillLevel;
		}
		
		/**
		 * Retrieves the remaining energy.
		 *
		 * @return the energy level
		 */
		public int getEnergyLevel() {
			return this.energyLevel;
		}
		
		/**
		 * Calculates the damage of an attack using the given weapon.
		 *
		 * @param w the weapon used for the attack
		 * @return the attack damage
		 */
		public int attack(Weapon w) {
			return this.skillLevel + w.getPower();
		}
		
		/**
		 * Applies incoming damage and lowers energy accordingly.
		 *
		 * @param attackAmount the damage received
		 * @return the damage applied
		 */
		public int hurt(int attackAmount) {
			this.energyLevel -= attackAmount;
			return attackAmount;
		}
		
		/**
		 * Checks whether this character has lost all energy.
		 *
		 * @return true if energy is zero or below
		 */
		public boolean isLose() {
			return this.energyLevel <= 0;
		}
	}

	/**
	 * Simple immutable weapon used within the duel.
	 */
	private static class Weapon {
		private final String name;
		private final int power;
		
		/**
		 * Builds a weapon with the provided name and power.
		 *
		 * @param name the weapon name
		 * @param power the weapon power
		 */
		public Weapon(String name, int power) {
			this.name = name;
			this.power = power;
		}
		
		/**
		 * Retrieves the weapon name.
		 *
		 * @return the name
		 */
		public String getName() {
			return this.name;
		}
		
		/**
		 * Retrieves the weapon power rating.
		 *
		 * @return the power value
		 */
		public int getPower() {
			return this.power;
		}
	}
	
}