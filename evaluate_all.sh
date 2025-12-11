#!/bin/bash

echo "Evaluating all qFunction models, picking best by wins then utility)..."

BEST_MODEL=""
BEST_WINS=-1
BEST_UTIL=-999999

BEST_INDEX=""
BEST_CYCLE=""

for f in params/qFunction*.model; do
    # Skip if no files match
    if [[ ! -e "$f" ]]; then
        echo "No qFunction model files found in params/."
        exit 1
    fi

    # Run each evaluation in a NEW JVM to prevent memory buildup
    OUTPUT=$(java -Xmx2G -cp "./lib/*:." edu.bu.pas.pokemon.Train \
        -p 1 \
        -t 1 \
        -v 30 \
        -i "$f" \
        src.pas.pokemon.agents.PolicyAgent \
        src.pas.pokemon.agents.AggroAgent 2>&1)

    # Extract avg(num_wins) and avg(utility)
    WINS=$(echo "$OUTPUT" | grep -o "avg(num_wins)=[-0-9.]*" | head -n 1 | sed 's/avg(num_wins)=//')
    UTIL=$(echo "$OUTPUT" | grep -o "avg(utility)=[-0-9.]*" | head -n 1 | sed 's/avg(utility)=//')

    # Error handling: if we couldn't parse wins, skip this model
    if [[ -z "$WINS" ]]; then
        echo "⚠️  Java crashed or no avg(num_wins) found for $f — skipping."
        continue
    fi

    # If we couldn't parse utility, treat as very bad utility
    if [[ -z "$UTIL" ]]; then
        UTIL=-999999
    fi

    # Initialize best with the first valid model
    if [[ -z "$BEST_MODEL" ]]; then
        BEST_MODEL="$f"
        BEST_WINS="$WINS"
        BEST_UTIL="$UTIL"
        # Try to parse the index from filename qFunctionN.model
        NAME=$(basename "$f")
        INDEX=$(echo "$NAME" | sed -E 's/[^0-9]*([0-9]+)\.model/\1/')
        BEST_INDEX="$INDEX"
        if [[ -n "$INDEX" ]]; then
            BEST_CYCLE=$((INDEX - 1))
        fi
        continue
    fi

    # Compare wins first
    greater_wins=$(echo "$WINS > $BEST_WINS" | bc -l)
    equal_wins=$(echo "$WINS == $BEST_WINS" | bc -l)

    if [[ "$greater_wins" -eq 1 ]]; then
        # Strictly better wins
        BEST_MODEL="$f"
        BEST_WINS="$WINS"
        BEST_UTIL="$UTIL"
        NAME=$(basename "$f")
        INDEX=$(echo "$NAME" | sed -E 's/[^0-9]*([0-9]+)\.model/\1/')
        BEST_INDEX="$INDEX"
        if [[ -n "$INDEX" ]]; then
            BEST_CYCLE=$((INDEX - 1))
        fi
    elif [[ "$equal_wins" -eq 1 ]]; then
        # Wins tie → break tie on utility
        greater_util=$(echo "$UTIL > $BEST_UTIL" | bc -l)
        if [[ "$greater_util" -eq 1 ]]; then
            BEST_MODEL="$f"
            BEST_WINS="$WINS"
            BEST_UTIL="$UTIL"
            NAME=$(basename "$f")
            INDEX=$(echo "$NAME" | sed -E 's/[^0-9]*([0-9]+)\.model/\1/')
            BEST_INDEX="$INDEX"
            if [[ -n "$INDEX" ]]; then
                BEST_CYCLE=$((INDEX - 1))
            fi
        fi
    fi
done

echo ""
echo "======================================"
if [[ -z "$BEST_MODEL" ]]; then
    echo "No valid models were evaluated (no scores found)."
else
    echo " BEST MODEL FILE: $BEST_MODEL"
    if [[ -n "$BEST_INDEX" ]]; then
        echo " (parsed index N = $BEST_INDEX → training cycle ≈ N - 1 = $BEST_CYCLE )"
    fi
    echo " avg(num_wins) = $BEST_WINS"
    echo " avg(utility)  = $BEST_UTIL"
fi
echo "======================================"

