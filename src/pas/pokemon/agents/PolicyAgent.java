package src.pas.pokemon.agents;


// SYSTEM IMPORTS
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.List;

import edu.bu.pas.pokemon.agents.NeuralQAgent;
import edu.bu.pas.pokemon.agents.senses.SensorArray;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.enums.Stat;
import edu.bu.pas.pokemon.linalg.Matrix;
import edu.bu.pas.pokemon.nn.Model;
import edu.bu.pas.pokemon.nn.models.Sequential;
import edu.bu.pas.pokemon.nn.layers.Dense; // fully connected layer
import edu.bu.pas.pokemon.nn.layers.ReLU;  // some activations (below too)
import edu.bu.pas.pokemon.nn.layers.Tanh;
import edu.bu.pas.pokemon.nn.layers.Sigmoid;


// JAVA PROJECT IMPORTS
import src.pas.pokemon.senses.CustomSensorArray;


public class PolicyAgent
    extends NeuralQAgent
{
    private int moveCount = 0;

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

        System.out.println("[PolicyAgent] initialize() called.");
        System.out.println("[PolicyAgent] Log file will be: " +
            new java.io.File("training_stats.log").getAbsolutePath());
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
        // TODO: change this to something more intelligent!

        TeamView myTeam = this.getMyTeamView(view);
        int bestIdx = -1;
        int bestHP = -1;

        for(int i=0; i<myTeam.size(); i++)
        {
            PokemonView pokemon = myTeam.getPokemonView(i);

            if(!pokemon.hasFainted())
            {
                int currHP = pokemon.getCurrentStat(Stat.HP);

                if(currHP>bestHP)
                {
                    bestHP = currHP;
                    bestIdx = i;
                }
            }
        }

        if(bestIdx>=0)
        {
            return bestIdx;
        }
        else
        {
            return null; 
        }
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

            return moves.get((int)(Math.random() * moves.size()));
        }

        double epsilon = 0;
        // if(epsilon<0.05)
        //     epsilon = 0.05;
        TeamView myTeam = this.getMyTeamView(view);
        PokemonView active = myTeam.getActivePokemonView();
        List<MoveView> moves = active.getAvailableMoves();

        if(moves==null || moves.isEmpty())
            return this.argmax(view);

        if(Math.random()<epsilon)
        {
            int r = (int)(Math.random() * moves.size());
            return moves.get(r);
        }

        return this.argmax(view);
    }

    @Override
    public void afterGameEnds(BattleView view)
    {

    }

    public TeamView getOpponentTeamView(BattleView view)
    {
        int myIdx = this.getMyTeamIdx(); 
        int oppIdx = (myIdx+1) % 2;   
        return view.getTeamView(oppIdx);
    }
}

