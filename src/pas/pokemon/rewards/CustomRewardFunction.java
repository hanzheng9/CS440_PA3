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


public class CustomRewardFunction
    extends RewardFunction
{

    public CustomRewardFunction()
    {
        super(RewardType.STATE); // currently configured to produce rewards as a function of the state
    }

    public double getLowerBound()
    {
        // TODO: change this. Reward values must be finite!
        return -1.0;
    }

    public double getUpperBound()
    {
        // TODO: change this. Reward values must be finite!
        return 1.0;
    }

    public double getStateReward(final BattleView state)
    {
        if(state==null) 
            return 0.0;
        TeamView myTeam = state.getTeam1View();
        TeamView oppTeam = state.getTeam2View();
        double myHPFrac = getTeamHPFraction(myTeam);
        double oppHPFrac = getTeamHPFraction(oppTeam);
        double diff = (myHPFrac - oppHPFrac);
        double terminalBonus = 0.0;

        if(state.isOver())
        {
            if(myHPFrac>oppHPFrac)
                terminalBonus = 1.0;      
            else if(myHPFrac<oppHPFrac)
                terminalBonus = -1.0;   
            else
                terminalBonus = 0.0;      
        }

        double rewards = diff + terminalBonus;

        if(rewards > getUpperBound())
            rewards = getUpperBound();
        if(rewards < getLowerBound())
            rewards = getLowerBound();

        return rewards;
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
        return getStateReward(nextState);
    }
}
