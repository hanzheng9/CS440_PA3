#!/bin/bash

echo "Evaluating all qFunction models safely (fresh JVM per model)..."

BEST_MODEL=""
BEST_SCORE=-999

for f in params/qFunction*.model; do
    echo "---------------------------------"
    echo "Testing $f ..."

    # Run each evaluation in a NEW JVM to prevent memory buildup
    OUTPUT=$(java -Xmx2G -cp "./lib/*:." edu.bu.pas.pokemon.Train \
        -p 1 \
        -t 1 \
        -v 30 \
        -i "$f" \
        src.pas.pokemon.agents.PolicyAgent \
        src.pas.pokemon.agents.AggroAgent 2>&1)

    # Extract avg(num_wins)
    SCORE=$(echo "$OUTPUT" | grep -o "avg(num_wins)=[0-9.\-]*" | sed 's/avg(num_wins)=//')

    # Error handling
    if [[ -z "$SCORE" ]]; then
        echo "⚠️  Java crashed or no score found for $f"
        continue
    fi

    echo "Result: avg(num_wins) = $SCORE"

    # Compare with best
    greater=$(echo "$SCORE > $BEST_SCORE" | bc -l)
    if [[ "$greater" -eq 1 ]]; then
        BEST_SCORE=$SCORE
        BEST_MODEL=$f
    fi
done

echo ""
echo "======================================"
echo " BEST MODEL: $BEST_MODEL"
echo " avg_wins = $BEST_SCORE"
echo "======================================"
