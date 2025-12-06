#!/bin/bash

echo "Evaluating all qFunction models..."
echo ""

BEST_MODEL=""
BEST_SCORE=-999

# Sort models numerically: qFunction2 < qFunction10 < qFunction100
MODEL_LIST=$(ls params/qFunction*.model | sed 's/[^0-9]*//g' | sort -n)

for num in $MODEL_LIST; do
    f="params/qFunction${num}.model"

    echo "---------------------------------"
    echo "Testing $f ..."

    # Run evaluation with larger heap + timeout to prevent stuck processes
    OUTPUT=$(timeout 20s java -Xmx4G -cp "./lib/*:." edu.bu.pas.pokemon.Train \
        -p 1 \
        -t 1 \
        -v 50 \
        -i "$f" \
        src.pas.pokemon.agents.PolicyAgent \
        src.pas.pokemon.agents.AggroAgent 2>&1)

    # If Java was killed or timed out
    if [[ $? -ne 0 ]]; then
        echo "⚠️  Java crashed or timed out for $f"
        continue
    fi

    SCORE=$(echo "$OUTPUT" | grep -o "avg(num_wins)=[0-9.\-]*" | sed 's/avg(num_wins)=//')

    echo "Result: avg(num_wins) = $SCORE"

    if [[ -z "$SCORE" ]]; then
        echo "⚠️  No valid score found for $f"
        continue
    fi

    greater=$(echo "$SCORE > $BEST_SCORE" | bc -l)
    if [[ "$greater" -eq 1 ]]; then
        BEST_SCORE=$SCORE
        BEST_MODEL=$f
    fi

    # Manual memory cleanup
    sync; sleep 0.1
done

echo ""
echo "======================================"
echo "BEST MODEL: $BEST_MODEL   (avg_wins = $BEST_SCORE)"
echo "======================================"