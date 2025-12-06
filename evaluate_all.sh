#!/bin/bash

echo "Evaluating all qFunction models..."

BEST_MODEL=""
BEST_SCORE=-999

for f in params/qFunction*.model; do
    echo ""
    echo "Testing $f ..."

    # Run evaluation (1 cycle, 1 training game, 50 eval games)
    OUTPUT=$(java -cp "./lib/*:." edu.bu.pas.pokemon.Train \
        -p 1 \
        -t 1 \
        -v 50 \
        -i "$f" \
        src.pas.pokemon.agents.PolicyAgent \
        src.pas.pokemon.agents.AggroAgent 2>&1)

    # Try to extract avg(num_wins)=X
    SCORE=$(echo "$OUTPUT" | grep -o "avg(num_wins)=[0-9.\-]*" | sed 's/avg(num_wins)=//')

    echo "Result: avg(num_wins) = $SCORE"

    # Skip if empty
    if [[ -z "$SCORE" ]]; then
        echo "⚠️  No valid score found for $f"
        continue
    fi

    # Compare to best score
    greater=$(echo "$SCORE > $BEST_SCORE" | bc -l)
    if [[ "$greater" -eq 1 ]]; then
        BEST_SCORE=$SCORE
        BEST_MODEL=$f
    fi
done

echo ""
echo "======================================"
echo "BEST MODEL: $BEST_MODEL   (avg_wins = $BEST_SCORE)"
echo "======================================"
