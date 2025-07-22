import sys
import json
from datetime import datetime

# Mapping Finderz query columns to your data keys
FIELD_MAPPING = {
    'nom': 'eventName',
    'date': 'eventDate',
    'description': 'eventDescription',
    'image': 'eventImageUrl',
    'link': 'eventLink'
}

def parse_date(s):
    return datetime.strptime(s, '%d-%m-%Y')

def process_command(command, data):
    tokens = command.strip().split()
    if not tokens:
        return

    action = tokens[0]
    idx = 1

    # Parse requested columns
    columns = []
    while idx < len(tokens) and tokens[idx] not in ['DE', 'PROVENANT', 'DATE', 'OUTPUT']:
        if tokens[idx] != ',':
            cols = tokens[idx].split(',')
            for c in cols:
                if c:
                    columns.append(c.strip())
        idx += 1

    # Initialize date filter
    date_filter = None

    # Parse filters (only DATE implemented here)
    while idx < len(tokens):
        if tokens[idx] == 'DATE':
            idx += 1
            if idx < len(tokens):
                if tokens[idx] == 'PLUS' and idx+2 < len(tokens) and tokens[idx+1] == 'RECENT' and tokens[idx+2] == 'QUE':
                    idx += 3
                    date_str = tokens[idx].strip("'")
                    date_filter = ('>', parse_date(date_str))
                    idx += 1
                elif tokens[idx] == 'ANTERIEUR' and idx+1 < len(tokens) and tokens[idx+1] == 'A':
                    idx += 2
                    date_str = tokens[idx].strip("'")
                    date_filter = ('<', parse_date(date_str))
                    idx += 1
                elif tokens[idx] == 'ENTRE' and idx+3 < len(tokens):
                    idx += 1
                    d1 = parse_date(tokens[idx].strip("'"))
                    idx += 2  # skip '<>'
                    d2 = parse_date(tokens[idx].strip("'"))
                    date_filter = ('between', d1, d2)
                    idx += 1
                else:
                    date_str = tokens[idx].strip("'")
                    date_filter = ('=', parse_date(date_str))
                    idx += 1
        else:
            idx += 1

    # Filter data
    results = []
    for item in data:
        # Filter by date if needed
        if date_filter:
            item_date_str = item.get('eventDate')
            if not item_date_str:
                continue  # skip items without a date
            try:
                item_date = parse_date(item_date_str)
            except ValueError:
                continue  # skip invalid dates

            op = date_filter[0]
            if op == '>' and not (item_date > date_filter[1]):
                continue
            elif op == '<' and not (item_date < date_filter[1]):
                continue
            elif op == '=' and not (item_date == date_filter[1]):
                continue
            elif op == 'between' and not (date_filter[1] <= item_date <= date_filter[2]):
                continue

        # Select requested columns
        if columns == ['*']:
            results.append(item)
        else:
            selected = {}
            for col in columns:
                field = FIELD_MAPPING.get(col, col)
                selected[col] = item.get(field, '')
            results.append(selected)

    # Execute action
    if action == 'AFFICHER':
        print(f"--- Affichage ---")
        for r in results:
            print(r)
    elif action == 'IMPRIMER':
        with open('output.csv', 'w', encoding='utf-8') as f:
            for r in results:
                f.write(str(r) + '\n')
        print("Imprimé vers output.csv")

def interpreter(query, data_json):
    lines = query.strip().split('\n')
    if lines[0].strip() != 'START' or lines[-1].strip() != 'END':
        print("Erreur : Requête doit commencer par START et finir par END")
        return

    data = json.loads(data_json)
    for line in lines[1:-1]:
        if line.strip():
            process_command(line, data)

if __name__ == "__main__":
    full_input = sys.stdin.read()

    if '---DATA---' not in full_input:
        print("Erreur : données manquantes")
        sys.exit(1)

    query, data_json = full_input.split('---DATA---', 1)
    interpreter(query, data_json)
