import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Simulation of a security guard and student duel that also validates field encapsulation.
 */
public class OfficeCombat2 {
	
	// To test if all fields in a class is not public
	/**
	 * Tests whether every declared field of the supplied class is private.
	 *
	 * @param clazz the class to inspect
	 * @return true if each declared field is private, false otherwise
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
	 * Runs the combat simulation by reading character and weapon data from standard input.
	 *
	 * @param args unused command line arguments
	 * @throws IOException if reading user input fails
	 */
	public static void main(String[] args) throws IOException {
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader inData = new BufferedReader(isr);
			
		// Input combat data
		String c1_info[] = inData.readLine().split(" ");
		String c2_info[] = inData.readLine().split(" ");
		String w1_info[] = inData.readLine().split(" ");
		String w2_info[] = inData.readLine().split(" ");
		
		SecurityGuard c1 = new SecurityGuard(c1_info[0], Integer.valueOf(c1_info[2]), Integer.valueOf(c1_info[1]));
		Student c2 = new Student(c2_info[0], Integer.valueOf(c2_info[2]), Integer.valueOf(c2_info[1]));
		SuperGun w1 = new SuperGun(w1_info[0], Integer.valueOf(w1_info[1]));
		BadGun w2 = new BadGun(w2_info[0], Integer.valueOf(w2_info[1]));;
		
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
				System.out.println(c1.getName() + " makes an attack by " + w1.getName() + "!");
				
				int hurtAmount = c2.hurt(attackAmount);
				if (hurtAmount == 0) {
					System.out.println(c2.getName() + " hides from the attack!");
				}
				else {
					System.out.println(c2.getName() + " takes a hurt amount of " + hurtAmount + "! Remaining energy becomes " + c2.getEnergyLevel() + ".");
				}
				
				if (round % 3 == 0) {
					c2.hide();
				}
			} 
			else {
				int attackAmount = c2.attack(w2);
				int hurtAmount = c1.hurt(attackAmount);
				
				System.out.println(c2.getName() + " makes an attack by " + w2.getName() + "!");
				System.out.println(c1.getName() + " takes a hurt amount of " + hurtAmount + "! Remaining energy becomes " + c1.getEnergyLevel() + ".");
				
				if (round % 3 == 0) {
					c1.boostWeapon(w1);
					System.out.println(c1.getName() + " boost the " + w1.getName() + "!");
				}
			}
			round++;
		}
		
		if (c1.isLose()) {
			System.out.println(c2.getName() + " wins! The examination paper is stolen!");
		}
		else {
			System.out.println(c1.getName() + " wins! The examination paper is secured!");
		}
		
		// Test if all class fields are private
		System.out.println("----------------------------");
		System.out.println("Test if all the class fields are private");
		System.out.println("Character: " + areAllFieldsPrivate(Character.class));
		System.out.println("Student: " + areAllFieldsPrivate(Student.class));
		System.out.println("SecurityGuard: " + areAllFieldsPrivate(SecurityGuard.class));
		System.out.println("Weapon: " + areAllFieldsPrivate(Weapon.class));
		System.out.println("SuperGun: " + areAllFieldsPrivate(SuperGun.class));
		System.out.println("BadGun: " + areAllFieldsPrivate(BadGun.class));
	}

	/**
	 * Base combatant definition containing shared behaviour and statistics.
	 */
	private static class Character {
		private final String name;
		private final int skillLevel;
		private int energyLevel;
		
		/**
		 * Creates a character with the specified identity, skill, and energy levels.
		 *
		 * @param name the character name
		 * @param skillLevel the base skill level
		 * @param energyLevel the starting energy pool
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
		 * Retrieves the current skill level.
		 *
		 * @return the skill level
		 */
		public int getSkillLevel() {
			return this.skillLevel;
		}
		
		/**
		 * Retrieves the remaining energy level.
		 *
		 * @return the energy level
		 */
		public int getEnergyLevel() {
			return this.energyLevel;
		}
		
		/**
		 * Calculates the raw attack amount when wielding the provided weapon.
		 *
		 * @param w the weapon being used
		 * @return the damage value inflicted
		 */
		public int attack(Weapon w) {
			return this.skillLevel + w.getPower();
		}
		
		/**
		 * Applies incoming damage and reduces energy accordingly.
		 *
		 * @param attackAmount the incoming damage amount
		 * @return the damage taken
		 */
		public int hurt(int attackAmount) {
			this.energyLevel -= attackAmount;
			return attackAmount;
		}
		
		/**
		 * Checks whether this character has run out of energy.
		 *
		 * @return true when the energy level is zero or below
		 */
		public boolean isLose() {
			return this.energyLevel <= 0;
		}
	}

	/**
	 * Student fighter that can hide to avoid incoming attacks.
	 */
	private static class Student extends Character {
		private boolean isHiding;
		
		/**
		 * Builds a student fighter with an optional hiding ability.
		 *
		 * @param name the student name
		 * @param skillLevel the base skill level
		 * @param energyLevel the starting energy
		 */
		public Student(String name, int skillLevel, int energyLevel) {
			super(name, skillLevel, energyLevel);
			this.isHiding = false;
		}
		
		/**
		 * Computes a student attack, penalising weak weapons slightly.
		 *
		 * @param w the weapon being used
		 * @return the effective damage amount
		 */
		@Override
		public int attack(Weapon w) {
			if (w instanceof BadGun) {
				return Math.max(0, super.attack(w) - 1);
			}
			return super.attack(w);
		}
		
		/**
		 * Applies incoming damage unless the student is currently hiding.
		 *
		 * @param attackAmount the incoming damage amount
		 * @return the damage taken (zero when hiding)
		 */
		@Override
		public int hurt(int attackAmount) {
			if (this.isHiding) {
				this.isHiding = false;
				return 0;
			}
			return super.hurt(attackAmount);
		}
		
		/**
		 * Enables the hiding state so the next hit can be dodged.
		 */
		public void hide() {
			this.isHiding = true;
		}
	}

	/**
	 * Security guard combatant that can boost super weapons.
	 */
	private static class SecurityGuard extends Character {
		/**
		 * Creates a security guard combatant with fixed skill and energy.
		 *
		 * @param name the guard name
		 * @param skillLevel the base skill level
		 * @param energyLevel the starting energy
		 */
		public SecurityGuard(String name, int skillLevel, int energyLevel) {
			super(name, skillLevel, energyLevel);
		}
		
		/**
		 * Delegates to the default attack behaviour.
		 *
		 * @param w the weapon being used
		 * @return the attack damage value
		 */
		@Override
		public int attack(Weapon w) {
			return super.attack(w);
		}
		
		/**
		 * Boosts the supplied weapon to increase future attack power.
		 *
		 * @param w the weapon to boost
		 */
		public void boostWeapon(SuperGun w) {
			w.boost();
		}
	}

	/**
	 * Generic weapon abstraction storing a name and power rating.
	 */
	private static class Weapon {
		private final String name;
		private int power;
		
		/**
		 * Establishes a weapon with a name and power rating.
		 *
		 * @param name the weapon name
		 * @param power the raw power rating
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
		 * Retrieves the current power rating.
		 *
		 * @return the power value
		 */
		public int getPower() {
			return this.power;
		}
		
		/**
		 * Updates the internal power rating.
		 *
		 * @param power the new power value
		 */
		protected void setPower(int power) {
			this.power = power;
		}
	}

	/**
	 * High-powered weapon that can be boosted mid-fight.
	 */
	private static class SuperGun extends Weapon {
		/**
		 * Builds a super gun with the given name and power.
		 *
		 * @param name the weapon name
		 * @param power the initial power rating
		 */
		public SuperGun(String name, int power) {
			super(name, power);
		}
		
		/**
		 * Doubles the weapon power to strengthen subsequent attacks.
		 */
		public void boost() {
			setPower(getPower() * 2);
		}
	}

	/**
	 * Weak weapon that leaves the wielder slightly disadvantaged.
	 */
	private static class BadGun extends Weapon {
		/**
		 * Builds a weak gun with the given name and power.
		 *
		 * @param name the weapon name
		 * @param power the initial power rating
		 */
		public BadGun(String name, int power) {
			super(name, power);
		}
	}
}