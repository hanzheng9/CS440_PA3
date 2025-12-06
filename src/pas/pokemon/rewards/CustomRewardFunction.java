package src.pas.pokemon.rewards;


// SYSTEM IMPORTS


// JAVA PROJECT IMPORTS
import edu.bu.pas.pokemon.agents.rewards.RewardFunction;
import edu.bu.pas.pokemon.agents.rewards.RewardFunction.RewardType;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.enums.Stat;
import edu.bu.pas.pokemon.core.Move.Category;
import src.pas.pokemon.senses.CustomSensorArray;


public class CustomRewardFunction
    extends RewardFunction
{

    public CustomRewardFunction()
    {
        super(RewardType.STATE_ACTION_STATE);
    }

    public double getLowerBound()
    {
        return -3.0;
    }

    public double getUpperBound()
    {
        return 3.0;
    }

    public double getStateReward(final BattleView state)
    {
        if(state==null)
            return 0.0;
        TeamView myTeam = state.getTeam1View();
        TeamView oppTeam = state.getTeam2View();
        double myHPFrac = getTeamHPFraction(myTeam);
        double oppHPFrac = getTeamHPFraction(oppTeam);
        double value = myHPFrac - oppHPFrac;

        if(value>getUpperBound())
            value = getUpperBound();
        if(value<getLowerBound())
            value = getLowerBound();

        return value;
    }

    private double getTeamHPFraction(final TeamView team) 
    {
        if(team==null || team.size()==0) 
            return 0.0;
        double current = 0.0;
        double max = 0.0;

        for(int i=0; i<team.size(); i++) 
        {
            PokemonView pokemon = team.getPokemonView(i);
            if(pokemon==null) 
                continue;
            int initialHp = pokemon.getInitialStat(Stat.HP);
            int currentHp = pokemon.getCurrentStat(Stat.HP);

            if(initialHp<0) 
                initialHp = 0;
            if(currentHp<0) 
                currentHp = 0;

            max += initialHp;
            current += currentHp;
        }

        if(max<=0.0) 
            return 0.0;

        return current/max;
    }

    public double getStateActionReward(final BattleView state,
                                       final MoveView action)
    {
        return getStateReward(state);
    }

    public double getStateActionStateReward(final BattleView state,
                                            final MoveView action,
                                            final BattleView nextState)
    {
        if(state==null || nextState==null)
            return 0.0;

        TeamView myTeamBefore = state.getTeam1View();
        TeamView oppTeamBefore = state.getTeam2View();
        TeamView myTeamAfter = nextState.getTeam1View();
        TeamView oppTeamAfter = nextState.getTeam2View();
        double myHPBefore = getTeamHPFraction(myTeamBefore);
        double oppHPBefore = getTeamHPFraction(oppTeamBefore);
        double myHPAfter = getTeamHPFraction(myTeamAfter);
        double oppHPAfter = getTeamHPFraction(oppTeamAfter);
        double reward = 0.0;

        reward += 10.0 * (oppHPBefore - oppHPAfter);
        reward -= 10.0 * (myHPBefore - myHPAfter);

        if(!nextState.isOver())
            reward -= 0.01;

        if(nextState.isOver())
        {
            if(myHPAfter>oppHPAfter)
                reward += 1.0;
            else if(myHPAfter<oppHPAfter)
                reward -= 1.0;
        }

        if(oppHPAfter==0 && oppHPBefore>0)
            reward += 0.5;
        if(myHPAfter==0 && myHPBefore>0)
            reward -= 0.5;

        if(reward > getUpperBound())
            reward = getUpperBound();
        if(reward < getLowerBound())
            reward = getLowerBound();

        return reward;
    }
}
