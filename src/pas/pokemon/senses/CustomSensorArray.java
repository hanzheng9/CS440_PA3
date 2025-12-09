package src.pas.pokemon.senses;


// SYSTEM IMPORTS


// JAVA PROJECT IMPORTS
import edu.bu.pas.pokemon.agents.senses.SensorArray;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.linalg.Matrix;
import edu.bu.pas.pokemon.core.enums.NonVolatileStatus;
import edu.bu.pas.pokemon.core.enums.Stat;
import edu.bu.pas.pokemon.core.enums.Type;
import edu.bu.pas.pokemon.core.Move.Category;


public class CustomSensorArray
    extends SensorArray
{

    // TODO: make fields if you want!

    public CustomSensorArray()
    {
        // TODO: intialize those fields if you make any!
    }

    public Matrix getSensorValues(final BattleView state, final MoveView action)
    {
        // TODO: Convert a BattleView and a MoveView into a row-vector containing measurements for every sense
        // you want your neural network to have. This method should be called if your model is a q-based model
        TeamView myTeam = state.getTeam1View();
        TeamView oppTeam = state.getTeam2View();
        PokemonView my = myTeam.getActivePokemonView();
        PokemonView opp = oppTeam.getActivePokemonView();
        double[] f = new double[64]; // features
        int i = 0;

        // HP
        f[i++] = my.getCurrentStat(Stat.HP)/(double)my.getInitialStat(Stat.HP);
        f[i++] = opp.getCurrentStat(Stat.HP)/(double)opp.getInitialStat(Stat.HP);

        // stats
        f[i++] = my.getCurrentStat(Stat.ATK)/255.0;
        f[i++] = my.getCurrentStat(Stat.SPATK)/255.0;
        f[i++] = my.getCurrentStat(Stat.SPD)/255.0;
        f[i++] = opp.getCurrentStat(Stat.ATK)/255.0;
        f[i++] = opp.getCurrentStat(Stat.SPATK)/255.0;
        f[i++] = opp.getCurrentStat(Stat.SPD)/255.0;

        if(my.getCurrentStat(Stat.SPD)>opp.getCurrentStat(Stat.SPD))
            f[i++] = 1.0;
        else
            f[i++] = 0.0;

        double dmgFlag = 0.0;
        if(action!=null)
        {
            if(action.getCategory()!=Category.STATUS)
                dmgFlag = 1.0;
        }
        f[i++] = dmgFlag;

        if(my.getCurrentType1()==Type.NORMAL || my.getCurrentType2()==Type.NORMAL)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.FIGHTING || my.getCurrentType2()==Type.FIGHTING)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.FLYING || my.getCurrentType2()==Type.FLYING)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.POISON || my.getCurrentType2()==Type.POISON)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.GROUND || my.getCurrentType2()==Type.GROUND)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.ROCK || my.getCurrentType2()==Type.ROCK)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.BUG || my.getCurrentType2()==Type.BUG)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.GHOST || my.getCurrentType2()==Type.GHOST)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.FIRE || my.getCurrentType2()==Type.FIRE)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.WATER || my.getCurrentType2()==Type.WATER)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.GRASS || my.getCurrentType2()==Type.GRASS)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.ELECTRIC || my.getCurrentType2()==Type.ELECTRIC)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.PSYCHIC || my.getCurrentType2()==Type.PSYCHIC)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.ICE || my.getCurrentType2()==Type.ICE)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(my.getCurrentType1()==Type.DRAGON || my.getCurrentType2()==Type.DRAGON)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.NORMAL || opp.getCurrentType2()==Type.NORMAL)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.FIGHTING || opp.getCurrentType2()==Type.FIGHTING)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.FLYING || opp.getCurrentType2()==Type.FLYING)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.POISON || opp.getCurrentType2()==Type.POISON)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.GROUND || opp.getCurrentType2()==Type.GROUND)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.ROCK || opp.getCurrentType2()==Type.ROCK)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.BUG || opp.getCurrentType2()==Type.BUG)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.GHOST || opp.getCurrentType2()==Type.GHOST)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.FIRE || opp.getCurrentType2()==Type.FIRE)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.WATER || opp.getCurrentType2()==Type.WATER)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.GRASS || opp.getCurrentType2()==Type.GRASS)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.ELECTRIC || opp.getCurrentType2()==Type.ELECTRIC)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.PSYCHIC || opp.getCurrentType2()==Type.PSYCHIC)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.ICE || opp.getCurrentType2()==Type.ICE)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;
        if(opp.getCurrentType1()==Type.DRAGON || opp.getCurrentType2()==Type.DRAGON)
            f[i++] = 1.0;
        else 
            f[i++] = 0.0;


        double myPar = 0.0;
        if(my.getNonVolatileStatus()==NonVolatileStatus.PARALYSIS)
            myPar = 1.0;
        f[i++] = myPar;

        double mySleep = 0.0;
        if(my.getNonVolatileStatus()==NonVolatileStatus.SLEEP)
            mySleep = 1.0;
        f[i++] = mySleep;

        double myFreeze = 0.0;
        if(my.getNonVolatileStatus()==NonVolatileStatus.FREEZE)
            myFreeze = 1.0;
        f[i++] = myFreeze;

        double oppPar = 0.0;
        if(opp.getNonVolatileStatus()==NonVolatileStatus.PARALYSIS)
            oppPar = 1.0;
        f[i++] = oppPar;

        double oppSleep = 0.0;
        if(opp.getNonVolatileStatus()==NonVolatileStatus.SLEEP)
            oppSleep = 1.0;
        f[i++] = oppSleep;

        double oppFreeze = 0.0;
        if(opp.getNonVolatileStatus()==NonVolatileStatus.FREEZE)
            oppFreeze = 1.0;
        f[i++] = oppFreeze;


        if(action != null)
        {
            double power = 0.0;
            if(action.getPower() != null)
                power = action.getPower()/200.0;
            f[i++] = power;

            double accuracy = 0.0;
            if(action.getAccuracy() != null)
                accuracy = action.getAccuracy()/100.0;
            f[i++] = accuracy;

            double priority = (action.getPriority()+1.0)/2.0;
            f[i++] = priority;

            double damage = 0.0;
            if(action.getCategory()!=Category.STATUS)
                damage = 1.0;
            f[i++] = damage;

            f[i++] = stab(my, action);
            f[i++] = superEffective(opp, action);
            f[i++] = notVeryEffective(opp, action);
        }

        Matrix m = Matrix.zeros(1, f.length);
        for(int j=0; j<f.length; j++)
            m.set(0, j, f[j]);

        return m;
    }

    public static double stab(PokemonView attacker, MoveView move)
    {
        if(move==null)
            return 0.0;
        Type moveType = move.getType();
        if(moveType==null)
            return 0.0;
        Type attackerType1 = attacker.getCurrentType1();
        Type attackerType2 = attacker.getCurrentType2();
        boolean typeMatches = false;

        if(attackerType1==moveType)
            typeMatches = true;

        if(attackerType2==moveType)
            typeMatches = true;

        if(typeMatches)
            return 1.0;

        return 0.0;
    }

    public static double superEffective(PokemonView defender, MoveView move)
    {
        if(move==null)
            return 0.0;
        Type moveType = move.getType();
        if(moveType==null)
            return 0.0;
        Type defenderType1 = defender.getCurrentType1();
        Type defenderType2 = defender.getCurrentType2();
        double multiplier = 1.0;

        multiplier *= typeEffectiveness(moveType, defenderType1);
        multiplier *= typeEffectiveness(moveType, defenderType2);

        if(multiplier>1.0)
            return 1.0;

        return 0.0;
    }

    public static double notVeryEffective(PokemonView defender, MoveView move)
    {
        if(move==null)
            return 0.0;
        Type moveType = move.getType();
        if(moveType == null)
            return 0.0;
        Type defenderType1 = defender.getCurrentType1();
        Type defenderType2 = defender.getCurrentType2();
        double multiplier = 1.0;

        multiplier *= typeEffectiveness(moveType, defenderType1);
        multiplier *= typeEffectiveness(moveType, defenderType2);

        if(multiplier<1.0)
            return 1.0;

        return 0.0;
    }

    public static double typeEffectiveness(Type moveType, Type targetType)
    {
        if(moveType==null || targetType==null)
            return 1.0;

        if(moveType==Type.FIRE && targetType==Type.GRASS)
            return 2.0;
        if(moveType==Type.WATER && targetType==Type.FIRE)
            return 2.0;
        if(moveType==Type.GRASS && targetType==Type.WATER)
            return 2.0;
        if(moveType==Type.FIGHTING && targetType==Type.NORMAL)
            return 2.0;
        if(moveType==Type.GRASS && targetType==Type.ROCK)
            return 2.0;
        if(moveType==Type.GRASS && targetType==Type.GROUND)
            return 2.0;
        if(moveType==Type.WATER && targetType==Type.ROCK)
            return 2.0;
        if(moveType==Type.WATER && targetType==Type.GROUND)
            return 2.0;

        if(moveType==Type.GRASS && targetType==Type.FIRE)
            return 0.5;
        if(moveType==Type.FIRE && targetType==Type.WATER)
            return 0.5;
        if(moveType==Type.WATER && targetType==Type.GRASS)
            return 0.5;
        if(moveType==Type.NORMAL && targetType==Type.ROCK)
            return 0.5;

        if(moveType==Type.NORMAL && targetType==Type.GHOST)
            return 0.0;
        if(moveType==Type.FIGHTING && targetType==Type.GHOST)
            return 0.0;

        return 1.0;
    }
}
