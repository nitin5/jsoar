/*
 * Copyright (c) 2008  Dave Ray <daveray@gmail.com>
 *
 * Created on Sep 13, 2008
 */
package org.jsoar.kernel.exploration;

import java.util.HashMap;
import java.util.Map;

import org.jsoar.kernel.Agent;
import org.jsoar.kernel.exploration.ExplorationParameter.ReductionPolicy;
import org.jsoar.kernel.learning.rl.LearningChoices;
import org.jsoar.kernel.learning.rl.ReinforcementLearning;
import org.jsoar.kernel.memory.Preference;
import org.jsoar.kernel.memory.PreferenceType;
import org.jsoar.kernel.memory.Slot;
import org.jsoar.kernel.symbols.DoubleSymbolImpl;
import org.jsoar.kernel.symbols.IntegerSymbolImpl;
import org.jsoar.kernel.symbols.SymbolImpl;
import org.jsoar.kernel.tracing.Trace;
import org.jsoar.kernel.tracing.Trace.Category;
import org.jsoar.util.adaptables.Adaptables;

/**
 * <em>This is an internal interface. Don't use it unless you know what you're doing.</em>
 * 
 * <p>exploration.cpp
 * 
 * @author ray
 */
public class Exploration
{
    /**
     * <p>kernel.h:147:ni_mode
     * @author ray
     */
    public static enum NumericIndifferentMode
    {
        NUMERIC_INDIFFERENT_MODE_AVG,
        NUMERIC_INDIFFERENT_MODE_SUM,
    }
    
    /**
     * Ways to Do User-Select
     * 
     * gsysparam.h:78:USER_SELECT_
     * 
     * @author ray
     */
    public static enum Policy
    {
        USER_SELECT_BOLTZMANN("boltzmann"),       /* boltzmann algorithm, with respect to temperature */
        USER_SELECT_E_GREEDY("epsilon-greedy"),       /* with probability epsilon choose random, otherwise greedy */
        USER_SELECT_FIRST("first"),       /* just choose the first candidate item */
        USER_SELECT_LAST("last"),       /* choose the last item   AGR 615 */    
        USER_SELECT_RANDOM("random-uniform"),       /* pick one at random */
        USER_SELECT_SOFTMAX("softmax");       /* pick one at random, probabalistically biased by numeric preferences */
        
        private final String policyName;
        
        private Policy(String policyName)
        {
            this.policyName = policyName;
        }
        
        /**
         * exploration.cpp:50:exploration_convert_policy
         * 
         * @return the policy name
         */
        public String getPolicyName()
        {
            return policyName;
        }
        
        /**
         * exploration.cpp:68:exploration_convert_policy
         * 
         * @param policyName
         * @return the policy, or {@code null} if not found
         */
        public static Policy findPolicy(String policyName)
        {
            for(Policy p : values())
            {
                if(p.policyName.equals(policyName))
                {
                    return p;
                }
            }
            return null;
        }
    }
    
    private final Agent context;
    private ReinforcementLearning rl;
    
    /**
     * USER_SELECT_MODE_SYSPARAM
     */
    private Policy userSelectMode = Policy.USER_SELECT_SOFTMAX;
    /**
     * USER_SELECT_REDUCE_SYSPARAM
     */
    private boolean autoUpdate = false;
    
    /**
     * <p>agent.h:748:numeric_indifferent_mode
     * <p>Initialized to NUMERIC_INDIFFERENT_MODE_SUM in create_soar_agent()
     */
    private NumericIndifferentMode numeric_indifferent_mode = NumericIndifferentMode.NUMERIC_INDIFFERENT_MODE_SUM;
    
    /**
     * Changed from array to map indexed by name
     * 
     * agent.h:752:exploration_params
     */
    private Map<String, ExplorationParameter> parameters = new HashMap<String, ExplorationParameter>();
    
    /**
     * @param context
     */
    public Exploration(Agent context)
    {
        this.context = context;
        
        // exploration initialization
        // agent.cpp:307:create_agent
        exploration_add_parameter( 0.1, new ExplorationValidateEpsilon(), "epsilon" );
        exploration_add_parameter( 25, new ExplorationValidateTemperature(), "temperature" );

    }
    
    public void initialize()
    {
        this.rl = Adaptables.adapt(context, ReinforcementLearning.class);
    }

    /**
     * exploration.cpp:89:exploration_set_policy
     * 
     * @param policy_name
     * @return true if the policy was set
     */
    public boolean exploration_set_policy(String policy_name)
    {   
        Policy policy = Policy.findPolicy( policy_name );
        
        if ( policy != null )
            return exploration_set_policy( policy );
        
        return false;
    }

    /**
     * <p>exploration.cpp:99:exploration_set_policy
     * 
     * @param policy
     * @return true if the policy was set
     */
    public boolean exploration_set_policy( Policy policy )
    {
        // TODO throw exception?
        if(policy != null)
        {
            userSelectMode = policy;
        }
        
        return false;
    }
    
    /**
     * <p>exploration.cpp:113:exploration_get_policy
     * 
     * @return the current policy
     */
    public Policy exploration_get_policy()
    {
        return userSelectMode;
    }
    
    /**
     * <p>exploration.cpp:121:exploration_add_parameter
     * 
     * @param value
     * @param val_func
     * @param name
     * @return the new parameter
     */
    public ExplorationParameter exploration_add_parameter( double value, ExplorationValueFunction val_func, String name )
    {
        // new parameter entry
        ExplorationParameter newbie = new ExplorationParameter();
        newbie.value = value;
        newbie.name = name;
        newbie.reduction_policy = ReductionPolicy.EXPLORATION_REDUCTION_EXPONENTIAL;
        newbie.val_func = val_func;
        newbie.rates.put(ReductionPolicy.EXPLORATION_REDUCTION_EXPONENTIAL, 1.0);
        newbie.rates.put(ReductionPolicy.EXPLORATION_REDUCTION_LINEAR, 0.0);
        
        parameters.put(name, newbie);
        
        return newbie;
    } 
    
    /**
     * <p>exploration.cpp:168:exploration_get_parameter_value
     * 
     * @param parameter
     * @return value of the parameter
     */
    double exploration_get_parameter_value(String parameter )
    {   
        ExplorationParameter param = parameters.get(parameter);
        return param != null ? param.value : 0.0;
    }  
    
    /**
     * <p>exploration.cpp:204:exploration_valid_parameter_value
     * 
     * @param name parameter name
     * @param value parameter value
     * @return true if the value is valid
     */
    boolean exploration_valid_parameter_value( String name, double value )
    {
        ExplorationParameter param = parameters.get( name );
        if ( param == null )
            return false;
        
        return param.val_func.call( value );
    }

    /**
     * <p>exploration.cpp:213:exploration_valid_parameter_value
     * 
     * @param parameter parameter object
     * @param value new value
     * @return true if the value is valid
     */
    boolean exploration_valid_parameter_value( ExplorationParameter parameter, double value )
    {
        if(parameter != null)
        {
            return parameter.val_func.call(value);
        }

        return false;
    }
    
    /**
     * <p>exploration.cpp:224:exploration_set_parameter_value
     * 
     * @param name parameter name
     * @param value new value
     * @return true if the parameter was set successfully
     */
    boolean exploration_set_parameter_value(String name, double value )
    {
        ExplorationParameter param = parameters.get( name );
        if ( param == null )
            return false;
        
        param.value = value;
        
        return true;
    }

    /**
     * <p>exploration.cpp:235:exploration_set_parameter_value
     * 
     * @param parameter the parameter object
     * @param value the new double value
     * @return true if the parameter was set successfully
     */
    boolean exploration_set_parameter_value(ExplorationParameter parameter, double value )
    {
        if(parameter != null)
        {
            parameter.value = value;
            return true;
        }
        return false;
    } 
    
    /**
     * <p>exploration.cpp:249:exploration_get_auto_update
     * 
     * @return true if auto update is enabled
     */
    boolean exploration_get_auto_update()
    {
        return autoUpdate;
    }

    /**
     * <p>exploration.cpp:257:exploration_set_auto_update
     * 
     * @param setting new auto update setting
     * @return true
     */
    boolean exploration_set_auto_update( boolean setting )
    {
        this.autoUpdate = setting;
        
        return true;
    }
    
    /**
     * <p>exploration.cpp:267:exploration_update_parameters
     */
    public void exploration_update_parameters()
    {   
        if ( exploration_get_auto_update( ) )
        {         
            for(ExplorationParameter p : parameters.values())
            {
                p.update();
            }
        }
    }
    
    /**
     * <p>exploration.cpp:322:exploration_get_reduction_policy
     * 
     * @param parameter parameter name
     * @return the reudction policy
     */
    ReductionPolicy exploration_get_reduction_policy( String parameter )
    {
        ExplorationParameter param = parameters.get(parameter);
        
        return param != null ? param.reduction_policy : null;
    }

    /**
     * <p>exploration.cpp:331:exploration_get_reduction_policy
     * 
     * @param parameter parameter object
     * @return reduction policy
     */
    ReductionPolicy exploration_get_reduction_policy( ExplorationParameter parameter )
    {
        return parameter != null ? parameter.reduction_policy : null;
    }
    
    /**
     * <p>exploration:375:exploration_set_reduction_policy
     * 
     * @param parameter parameter name
     * @param policy_name policy name
     * @return true if the reduction policy was set
     */
    boolean exploration_set_reduction_policy( String parameter, String policy_name )
    {
        ExplorationParameter param = parameters.get(parameter);
        if(param == null)
        {
            return false;
        }
        ReductionPolicy policy = ReductionPolicy.findPolicy(policy_name);
        
        if(policy == null)
        {
            return false;
        }
        
        param.reduction_policy = policy;
        
        return true;
    }

    /**
     * <p>exploration.cpp:468:exploration_set_reduction_rate
     * 
     * @param parameter parameter name
     * @param policy_name policy name
     * @param reduction_rate reduction rate
     * @return true if the reduction rate was set
     */
    boolean exploration_set_reduction_rate(String parameter, String policy_name, double reduction_rate )
    {
        ExplorationParameter param = parameters.get(parameter);
        if(param == null)
        {
            return false;
        }
        ReductionPolicy policy = ReductionPolicy.findPolicy(policy_name);
        if(policy == null)
        {
            return false;
        }
        return param.setReductionRate(policy, reduction_rate);
    }
    
    /**
     * <p>exploration.cpp:497:exploration_choose_according_to_policy
     * 
     * @param s the slot
     * @param candidates list of preference candidates, using {@link Preference#next_candidate}
     * @return the chosen preference
     */
    public Preference exploration_choose_according_to_policy(Slot s, Preference candidates)
    {
        Policy exploration_policy = exploration_get_policy();

        // get preference values for each candidate
        for ( Preference cand = candidates; cand != null; cand = cand.next_candidate )
            exploration_compute_value_of_candidate( cand, s, 0.0);

        final boolean my_rl_enabled = rl.rl_enabled();
        final LearningChoices my_learning_policy = my_rl_enabled ? context.getProperties().get(ReinforcementLearning.LEARNING_POLICY) : LearningChoices.Q;
        double top_value = candidates.numeric_value;
        boolean top_rl = candidates.rl_contribution;
        
        // should find highest valued candidate in q-learning
        if (my_rl_enabled && 
            my_learning_policy == LearningChoices.Q)
        {
            for ( Preference cand=candidates; cand!=null; cand=cand.next_candidate )
            {
                if ( cand.numeric_value > top_value )
                {
                    top_value = cand.numeric_value;
                    top_rl = cand.rl_contribution;
                }
            }
        }
        
        Preference return_val = null;
        switch ( exploration_policy )
        {
            case USER_SELECT_FIRST:
                return_val = candidates;
                break;
            
            case USER_SELECT_LAST:
                for (return_val = candidates; return_val.next_candidate != null; return_val = return_val.next_candidate);
                break;

            case USER_SELECT_RANDOM:
                return_val = exploration_randomly_select( candidates );
                break;

            case USER_SELECT_SOFTMAX:
                return_val = exploration_probabilistically_select( candidates );
                break;

            case USER_SELECT_E_GREEDY:
                return_val = exploration_epsilon_greedy_select( candidates );
                break;

            case USER_SELECT_BOLTZMANN:
                return_val = exploration_boltzmann_select( candidates );
                break;
        }

        // should perform update here for chosen candidate in sarsa
        // should perform update here for chosen candidate in sarsa 
        if ( my_rl_enabled )
        {
            rl.rl_tabulate_reward_values();

            if (my_learning_policy == LearningChoices.SARSA)
            {
                rl.rl_perform_update(return_val.numeric_value, return_val.rl_contribution, s.id );
            }
            else if (my_learning_policy == LearningChoices.Q)
            {
                rl.rl_perform_update(top_value, top_rl, s.id );

                if ( return_val.numeric_value != top_value )
                    ReinforcementLearning.rl_watkins_clear(s.id);
            }
        }
        
        return return_val;    
    }

    /**
     * <p>exploration.cpp:557:exploration_randomly_select
     */
    private Preference exploration_randomly_select( Preference candidates )
    {
        // select at random 
        int cand_count = Preference.countCandidates(candidates);
        int chosen_num = context.getRandom().nextInt(cand_count);
        //chosen_num = SoarRandInt( cand_count - 1 );
        
        return Preference.getCandidate(candidates, chosen_num);
    }

    /**
     * <p>exploration.cpp:582:exploration_probabilistically_select
     */
    private Preference exploration_probabilistically_select( Preference candidates )
    {   
        double total_probability = 0;

        // count up positive numbers
        for (Preference cand = candidates; cand != null; cand = cand.next_candidate )
            if ( cand.numeric_value > 0 )
                total_probability += cand.numeric_value;
        
        // if nothing positive, resort to random
        if ( total_probability == 0 )
            return exploration_randomly_select( candidates );
        
        // choose a random preference within the distribution
        double rn = context.getRandom().nextDouble(); // SoarRand();
        double selected_probability = rn * total_probability;
        double current_sum = 0;

        // select the candidate based upon the chosen preference
        for (Preference cand = candidates; cand != null; cand = cand.next_candidate ) 
        {
            if ( cand.numeric_value > 0 )
            {
                current_sum += cand.numeric_value;
                if ( selected_probability <= current_sum )
                    return cand;
            }
        }

        return null;
    }

    /**
     * <p>exploration.cpp:621:exploration_boltzmann_select
     */
    private Preference exploration_boltzmann_select( Preference candidates )
    {
        // TODO This is weird.
        double temp =  exploration_get_parameter_value("temperature" /* (const long) EXPLORATION_PARAM_TEMPERATURE */);
        
        // output trace information
        final Trace trace = context.getTrace();
        if ( trace.isEnabled(Category.INDIFFERENT))
        {
            for (Preference cand = candidates; cand != null; cand = cand.next_candidate )
            {
                trace.print("\n Candidate %s:  ", cand.value );
                trace.print("Value (Sum) = %f, (Exp) = %f", cand.numeric_value, 
                                    Math.exp( cand.numeric_value / temp ) );
                /*
                xml_begin_tag( my_agent, kTagCandidate );
                xml_att_val( my_agent, kCandidateName, cand->value );
                xml_att_val( my_agent, kCandidateType, kCandidateTypeSum );
                xml_att_val( my_agent, kCandidateValue, cand->numeric_value );
                xml_att_val( my_agent, kCandidateExpValue, exp( cand->numeric_value / temp ) );
                xml_end_tag( my_agent, kTagCandidate );
                */
            }
        }

        /**
         * Since we can't guarantee any combination of temperature/q-values, could be useful
         * to notify the user if double limit has been breached.
         */
        double exp_max = Math.log( Double.MAX_VALUE );
        double q_max = exp_max * temp;

        /*
         * method to increase usable range of boltzmann with double
         * - find the highest/lowest q-values
         * - take half the difference
         * - subtract this value from all q-values when making calculations
         * 
         * this maintains relative probabilities of selection, while reducing greatly the exponential extremes of calculations
         */
        double q_diff = 0;
        if ( candidates.next_candidate != null ) 
        {
            double q_high = candidates.numeric_value;
            double q_low = candidates.numeric_value;
            
            for (Preference cand = candidates.next_candidate; cand != null; cand = cand.next_candidate ) 
            {
                if ( cand.numeric_value > q_high )
                    q_high = cand.numeric_value;
                if ( cand.numeric_value < q_low )
                    q_low = cand.numeric_value;
            }

            q_diff = ( q_high - q_low ) / 2;
        } 
        else 
        {
            q_diff = candidates.numeric_value;
        }

        double total_probability = 0.0;
        for (Preference cand = candidates; cand != null; cand = cand.next_candidate) 
        {

            /*  Total Probability represents the range of values, we expect
             *  the use of negative valued preferences, so its possible the
             *  sum is negative, here that means a fractional probability
             */
            double q_val = ( cand.numeric_value - q_diff );
            total_probability += Math.exp( (double) (  q_val / temp ) );
            
            /**
             * Let user know if adjusted q-value will overflow
             */
            if ( q_val > q_max )
            {
                context.getPrinter().warn("WARNING: Boltzmann update overflow! %f > %f", q_val, q_max );
            }
        }

        double rn = context.getRandom().nextDouble(); //SoarRand(); // generates a number in [0,1]
        double selected_probability = rn * total_probability;

        double current_sum = 0.0;
        for (Preference cand = candidates; cand != null; cand = cand.next_candidate) 
        {
            current_sum += Math.exp( (double) ( ( cand.numeric_value - q_diff ) / temp ) );
            
            if ( selected_probability <= current_sum )
                return cand;
        }
        
        return null;
    }
    
    /**
     * <p>exploration.cpp:723:exploration_epsilon_greedy_select
     */
    private Preference exploration_epsilon_greedy_select(Preference candidates )
    {
        // TODO this seems weird
        double epsilon = exploration_get_parameter_value( "epsilon" /* (const long) EXPLORATION_PARAM_EPSILON */);

        final Trace trace = context.getTrace();
        if ( trace.isEnabled(Category.INDIFFERENT))
        {
            for (Preference cand = candidates; cand != null; cand = cand.next_candidate )
            {
                trace.print("\n Candidate %s:  Value (Sum) = %f", cand.value , cand.numeric_value );
            }
        }

        if ( context.getRandom().nextDouble() /*SoarRand()*/ < epsilon ) 
            return exploration_randomly_select( candidates );
        else
            return exploration_get_highest_q_value_pref( candidates );
    }
    
    /**
     * <p>exploration.cpp:752:exploration_get_highest_q_value_pref
     */
    private Preference exploration_get_highest_q_value_pref( Preference candidates )
    {
        Preference top_cand = candidates;
        double top_value = candidates.numeric_value;
        int num_max_cand = 0;

        for (Preference cand=candidates; cand!=null; cand=cand.next_candidate )
        {
            if ( cand.numeric_value > top_value ) 
            {
                top_value = cand.numeric_value;
                top_cand = cand;
                num_max_cand = 1;
            } 
            else if ( cand.numeric_value == top_value ) 
                num_max_cand++;
        }

        if ( num_max_cand == 1 )    
            return top_cand;
        else 
        {
            // if operators tied for highest Q-value, select among tied set at random
            int chosen_num = context.getRandom().nextInt(num_max_cand); //  SoarRandInt( num_max_cand - 1 );
            
            Preference cand = candidates;
            while ( cand.numeric_value != top_value ) 
                cand = cand.next_candidate;
            
            while ( chosen_num != 0) 
            {
                cand = cand.next_candidate;
                chosen_num--;
                
                while ( cand.numeric_value != top_value ) 
                    cand = cand.next_candidate;
            }
            
            return cand;
        }
    }
    
    /**
     * <p>exploration.cpp:798:exploration_compute_value_of_candidate
     * 
     * @param cand candidate preference
     * @param s  the slot
     * @param default_value default value to use (Defaults to 0.0 in CSoar)
     */
    public void exploration_compute_value_of_candidate(Preference cand, Slot s, double default_value )
    {
        if ( cand == null ) return;

        // initialize candidate values
        cand.total_preferences_for_candidate = 0;
        cand.numeric_value = 0;
        cand.rl_contribution = false;
        
        // all numeric indifferents
        for (Preference pref = s.getPreferencesByType(PreferenceType.NUMERIC_INDIFFERENT); 
             pref != null; pref = pref.next) 
        {
            if ( cand.value == pref.value )
            {
                cand.total_preferences_for_candidate += 1;
                cand.numeric_value += get_number_from_symbol( pref.referent );
                
                if(pref.inst.prod.rlRuleInfo != null)
                {
                    cand.rl_contribution = true;
                }
            }
        }

        // all binary indifferents
        for (Preference pref = s.getPreferencesByType(PreferenceType.BINARY_INDIFFERENT); 
             pref != null; pref = pref.next ) 
        {
            if (cand.value == pref.value)
            {
                cand.total_preferences_for_candidate += 1;
                cand.numeric_value += get_number_from_symbol( pref.referent );
            }
        }
        
        // if no contributors, provide default
        if ( cand.total_preferences_for_candidate == 0 ) 
        {
            cand.numeric_value = default_value;
            cand.total_preferences_for_candidate = 1;
        }
        
        // accomodate average mode
        if ( numeric_indifferent_mode == NumericIndifferentMode.NUMERIC_INDIFFERENT_MODE_AVG )
            cand.numeric_value = cand.numeric_value / cand.total_preferences_for_candidate;
    }

    /**
     * <p>misc.cpp:34:get_number_from_symbol
     * 
     * @param s the symbol
     * @return the double value of the symbol if it is numeric
     */
    public static double get_number_from_symbol(SymbolImpl s)
    {
        DoubleSymbolImpl f = s.asDouble();
        if(f != null)
        {
            return f.getValue();
        }
        IntegerSymbolImpl i = s.asInteger();
        if(i != null)
        {
            return i.getValue();
        }
        return 0.0;
    }
}
