package src.pas.pokemon.agents;


// SYSTEM IMPORTS
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.ArrayList;
import java.util.List;

import edu.bu.pas.pokemon.agents.NeuralQAgent;
import edu.bu.pas.pokemon.agents.senses.SensorArray;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.enums.Stat;
import edu.bu.pas.pokemon.core.enums.Type;
import edu.bu.pas.pokemon.linalg.Matrix;
import edu.bu.pas.pokemon.nn.Model;
import edu.bu.pas.pokemon.nn.models.Sequential;
import edu.bu.pas.pokemon.nn.layers.Dense; // fully connected layer
import edu.bu.pas.pokemon.nn.layers.Tanh;
import edu.bu.pas.pokemon.core.Move.Category;


// JAVA PROJECT IMPORTS
import src.pas.pokemon.senses.CustomSensorArray;


public class PolicyAgent
    extends NeuralQAgent
{
    private int gameCount = 0;
    private double epsilon = 0.2;

    public PolicyAgent()
    {
        super();
    }

    public void initializeSenses(Namespace args)
    {
        SensorArray modelSenses = new CustomSensorArray();

        this.setSensorArray(modelSenses);
    }

    @Override
    public void initialize(Namespace args)
    {
        // make sure you call this, this will call your initModel() and set a field
        // AND if the command line argument "inFile" is present will attempt to set
        // your model with the contents of that file.
        this.initializeSenses(args);
        super.initialize(args);

        // what senses will your neural network have?
        //this.initializeSenses(args);

        // do what you want just don't expect custom command line options to be available
        // when I'm testing your code
    }

    @Override
    public Model initModel()
    {
        // TODO: create your neural network

        Sequential qFunction = new Sequential();
        qFunction.add(new Dense(64, 32));
        qFunction.add(new Tanh());
        qFunction.add(new Dense(32, 16));
        qFunction.add(new Tanh());
        qFunction.add(new Dense(16, 1));

        return qFunction;
    }

    @Override
    public Integer chooseNextPokemon(BattleView view)
    {
        TeamView myTeam = this.getMyTeamView(view);
        TeamView oppTeam = this.getOpponentTeamView(view);
        PokemonView oppActive = oppTeam.getActivePokemonView();
        int bestIdx = -1;
        double bestScore = -999.0;

        for(int i=0; i<myTeam.size(); i++)
        {
            PokemonView pokemon = myTeam.getPokemonView(i);
            if(pokemon.hasFainted())
                continue;
            double hpScore = pokemon.getCurrentStat(Stat.HP)/(double)pokemon.getInitialStat(Stat.HP);
            double typeScore = typeEffectivenessScore(pokemon, oppActive);
            double speedScore = 0.0;

            if(pokemon.getCurrentStat(Stat.SPD)>oppActive.getCurrentStat(Stat.SPD))
                speedScore = 0.2;
            double statusPenalty = 0.0;
            if(pokemon.getNonVolatileStatus() != null)
                statusPenalty = -0.3;
            double lowHpPenalty = 0.0;
            if(hpScore < 0.30)
                lowHpPenalty = -0.5;
            double totalScore = hpScore + typeScore + speedScore + statusPenalty + lowHpPenalty;

            if(totalScore>bestScore)
            {
                bestScore = totalScore;
                bestIdx = i;
            }
        }

        if(bestIdx>=0)
            return bestIdx;
        else
            return null;
    }

    private double typeEffectivenessScore(PokemonView myPokemon, PokemonView oppPokemon)
    {
        Type myType1 = myPokemon.getCurrentType1();
        Type myType2 = myPokemon.getCurrentType2();
        Type oppType1 = oppPokemon.getCurrentType1();
        Type oppType2 = oppPokemon.getCurrentType2();
        double score = 0.0;

        score += simpleEffectiveness(myType1, oppType1);
        score += simpleEffectiveness(myType1, oppType2);
        score += simpleEffectiveness(myType2, oppType1);
        score += simpleEffectiveness(myType2, oppType2);

        return score;
    }

    private double simpleEffectiveness(Type attackingType, Type defendingType)
    {
        if(attackingType==null || defendingType==null)
            return 0.0;
        if(attackingType==Type.GRASS && defendingType==Type.WATER)
            return 0.3;
        if(attackingType==Type.WATER && defendingType==Type.FIRE)
            return 0.3;
        if(attackingType==Type.FIRE && defendingType==Type.GRASS)
            return 0.3;
        if(attackingType==Type.FIGHTING && defendingType==Type.NORMAL)
            return 0.3;
        if(attackingType==Type.GRASS && defendingType==Type.FIRE)
            return -0.3;
        if(attackingType==Type.WATER && defendingType==Type.GRASS)
            return -0.3;
        if(attackingType==Type.FIRE && defendingType==Type.WATER)
            return -0.3;
        if(attackingType==Type.NORMAL && defendingType==Type.GHOST)
            return -0.3;

        return 0.0;
    }

    @Override
    public MoveView getMove(BattleView view)
    {
        // TODO: change this to include random exploration during training and maybe use the transition model to make
        // good predictions?
        // if you choose to use the transition model you might want to also override the makeGroundTruth(...) method
        // to not use temporal difference learning

        // currently always tries to argmax the learned model
        // this is not a good idea to always do when training. When playing evaluation games you *do* want to always
        // argmax your model, but when training our model may not know anything yet! So, its a good idea to sometime
        // during training choose *not* to argmax the model and instead choose something new at random.

        // HOW that randomness works and how often you do it are up to you, but it *will* affect the quality of your
        // learned model whether you do it or not!
        if(this.getSensorArray()==null) 
        {
            TeamView myTeam = this.getMyTeamView(view);
            PokemonView active = myTeam.getActivePokemonView();
            List<MoveView> moves = active.getAvailableMoves();

            if(moves==null || moves.isEmpty())
                return null;

            return choosePreferredRandomMove(view);
        }

        TeamView myTeam = this.getMyTeamView(view);
        PokemonView active = myTeam.getActivePokemonView();
        List<MoveView> moves = active.getAvailableMoves();
        if(moves==null || moves.isEmpty())
            return null;

        List<MoveView> candidateMoves = getPreferredMoves(moves);

        if(Math.random()<epsilon)
        {
            int r = (int)(Math.random() * candidateMoves.size());
            return candidateMoves.get(r);
        }

        MoveView greedy = this.argmax(view);

        if(candidateMoves.contains(greedy))
            return greedy;

        MoveView bestPreferred = pickStrongest(candidateMoves);
        if(bestPreferred!=null)
            return bestPreferred;

        int r = (int)(Math.random() * candidateMoves.size());
        return candidateMoves.get(r);
    }

    private List<MoveView> getPreferredMoves(List<MoveView> moves)
    {
        List<MoveView> grassDamaging = new ArrayList<>();
        List<MoveView> damaging = new ArrayList<>();

        for(MoveView move: moves)
        {
            if(move==null)
                continue;

            if(move.getCategory()!=Category.STATUS)
                damaging.add(move);

            if(move.getCategory()!=Category.STATUS && move.getType()==Type.GRASS)
                grassDamaging.add(move);
        }

        if(!grassDamaging.isEmpty())
            return grassDamaging;    
        if(!damaging.isEmpty())
            return damaging;          
        return moves;                 
    }

   private MoveView pickStrongest(List<MoveView> moves)
    {
        MoveView best = null;
        int bestPower = -1;

        for(MoveView move: moves)
        {
            if(move==null)
                continue;
            Integer p = move.getPower();

            if(p==null)
                continue;
            if(p>bestPower)
            {
                bestPower = p;
                best = move;
            }
        }
        if(best!=null)
            return best;

        for(MoveView move: moves)
        {
            if(move!=null)
                return move;
        }
        return null;
    }

    private MoveView choosePreferredRandomMove(BattleView view)
    {
        TeamView myTeam = this.getMyTeamView(view);
        PokemonView active = myTeam.getActivePokemonView();
        List<MoveView> moves = active.getAvailableMoves();
        if(moves==null || moves.isEmpty())
            return null;

        List<MoveView> preferred = getPreferredMoves(moves);
        int r = (int)(Math.random() * preferred.size());
        return preferred.get(r);

    }

    @Override
    public void afterGameEnds(BattleView view)
    {
        gameCount++;
    }

    public TeamView getOpponentTeamView(BattleView view)
    {
        int myIdx = this.getMyTeamIdx(); 
        int oppIdx = (myIdx+1) % 2;   
        return view.getTeamView(oppIdx);
    }
}


