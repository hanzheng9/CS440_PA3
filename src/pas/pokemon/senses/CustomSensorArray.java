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
        if(action.getCategory()!=Category.STATUS)
            dmgFlag = 1.0;
        f[i++] = dmgFlag;

        Type[] all = {Type.NORMAL, Type.FIRE, Type.WATER, Type.GRASS, Type.ELECTRIC, Type.ICE, Type.FIGHTING, Type.POISON};

        for(int k=0; k<all.length; k++)
        {
            if(my.getCurrentType1()==all[k] || my.getCurrentType2()==all[k])
                f[i++] = 1.0;
            else
                f[i++] = 0.0;
        }

        for(int k=0; k<all.length; k++)
        {
            if(opp.getCurrentType1()==all[k] || opp.getCurrentType2()==all[k])
                f[i++] = 1.0;
            else
                f[i++] = 0.0;
        }

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

        if(action!=null)
        {
            double power = 0.0;
            if(action.getPower()!=null)
                power = action.getPower()/200.0;
            f[i++] = power;

            double accuracy = 0.0;
            if(action.getAccuracy()!=null)
                accuracy = action.getAccuracy()/100.0;
            f[i++] = accuracy;

            double priority = (action.getPriority()+1.0)/2.0;
            f[i++] = priority;

            double damage = 0.0;
            if(action.getCategory()!=Category.STATUS)
                damage = 1.0;
            f[i++] = damage;
        }

        Matrix m = Matrix.zeros(1, f.length);
        for(int j=0; j<f.length; j++)
            m.set(0, j, f[j]);

        return m;
    }
}
