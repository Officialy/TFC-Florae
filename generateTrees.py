import os

from nbtlib import nbt
from nbtlib.tag import *


def tree(origin, wood, nameout):
    f = nbt.load(origin + '.nbt')
    for block in f.root['palette']:

        if block['Name'] == 'minecraft:log':
            block['Name'] = String('tfc:wood/log/' + wood)
            prop = block['Properties']
            block['Properties'] = Compound({
                'small': String('false'),
                'placed': String('false'),
                'axis': prop['axis']
            })

        if block['Name'] == 'minecraft:planks':  # Planks indicate bark blocks
            block['Name'] = String('tfc:wood/log/' + wood)
            block['Properties'] = Compound({
                'small': String('false'),
                'placed': String('false'),
                'axis': String('none')
            })

        if block['Name'] == 'minecraft:leaves':
            block['Name'] = String('tfc:wood/leaves/' + wood)
            block['Properties'] = Compound({
                'decayable': String('true')
            })

    if not os.path.exists('src/main/resources/assets/tfc/structures/' + wood):
        os.makedirs('src/main/resources/assets/tfc/structures/' + wood)
    f.save('src/main/resources/assets/tfc/structures/' + wood + '/' + nameout + '.nbt')


def fruit_tree(ftree):
    f = nbt.load('structure_templates/fruit_tree_base.nbt')
    for block in f.root['palette']:
        if block['Name'] == 'tfc:fruit_trees/branch/peach':
            block['Name'] = String('tfc:fruit_trees/branch/' + ftree)
        elif block['Name'] == 'tfc:fruit_trees/leaves/peach':
            block['Name'] = String('tfc:fruit_trees/leaves/' + ftree)
        elif block['Name'] == 'tfc:fruit_trees/trunk/peach':
            block['Name'] = String('tfc:fruit_trees/trunk/' + ftree)

    if not os.path.exists('src/main/resources/assets/tfc/structures/fruit_trees'):
        os.makedirs('src/main/resources/assets/tfc/structures/fruit_trees')
    f.save('src/main/resources/assets/tfc/structures/fruit_trees/' + ftree + '.nbt')


WOOD_TYPES = {
    'alder': 'alder',
    'bamboo': 'bamboo',
    'baobab': 'baobab',
    'beech': 'beech',
    'cinnamon': 'cinnamon',
    'ebony': 'ebony',
    'eucalyptus': 'eucalyptus',
    'fever': 'fever',
    'fruitwood': 'fruitwood',
    'ginkgo': 'ginkgo',
    'larch': 'larch',
    'hawthorn': 'hawthorn',
    'juniper': 'juniper',
    'mahogany': 'mahogany',
    'poplar': 'poplar',
    'redwood': 'redwood',
    'rowan': 'rowan',
    'teak': 'teak',
    'white_elm': 'white_elm',
    'yew': 'yew'
}

for wood, key in WOOD_TYPES.items():

    # alder
    if key == 'alder':
        for s in ['1', '2', '3', '4', '5', '6', '7', '8', '9']:
            tree('structure_templates/alder/' + s, wood, s)

    # baobab
    if key == 'baobab':
        for s in ['1', '2']:
            tree('structure_templates/baobab/' + s, wood, s)

    # beech
    if key == 'beech':
        for s in ['1', '2', '3', '4', '5', '6', '7', '8', '9']:
            tree('structure_templates/beech/' + s, wood, s)

    # cinnamon
    if key == 'cinnamon':
        for s in ['1', '2', '3', '4']:
            tree('structure_templates/cinnamon/' + s, wood, s)

    # ebony
    if key == 'ebony':
        for s in ['1', '2', '3', '4', '5', '6']:
            tree('structure_templates/ebony/' + s, wood, s)

    # eucalyptus
    if key == 'eucalyptus':
        for s in ['1', '2', '3', '4']:
            tree('structure_templates/eucalyptus/' + s, wood, s)




    # normal (vanilla oak)
    if key == 'normal':
        tree('structure_templates/normal', wood, 'base')
        tree('structure_templates/normal_overlay', wood, 'overlay')

    # tall (tfc douglas fir, but smaller)
    if key == 'tall':
        tree('structure_templates/tall', wood, 'base')
        tree('structure_templates/tall_overlay', wood, 'overlay')

    # tallXL (tfc douglas fir, full size-ish)
    if key == 'tallXL':
        tree('structure_templates/tall2', wood, 'base')
        tree('structure_templates/tall2_overlay', wood, 'overlay')

    # overhang (willow)
    if key == 'willow':
        tree('structure_templates/willow', wood, 'base')
        tree('structure_templates/willow_overlay', wood, 'overlay')

    # conifer (vanilla spruce)
    if key == 'conifer':
        for s in ['1', '2', '3', '4', '5', '6', '7']:
            tree('structure_templates/conifer' + s, wood, s)

    # sequoia (large vanilla spruce kind of)
    if key == 'sequoia':
        for s in ['base', 'mid', 'top']:
            for t in ['1', '2', '3']:
                tree('structure_templates/conifer_large_' + s + t, wood, s + t)

    # acacia (vanilla acacia, bit bigger)
    if key == 'acacia':
        for s in ['1', '2', '3']:
            tree('structure_templates/acacia_branch' + s, wood, 'branch' + s)

    # palm like trees
    if key == 'tropical':
        for s in ['1', '2', '3', '4', '5', '6', '7']:
            tree('structure_templates/tropical' + s, wood, s)

    # kapok (vanilla jungle trees, but better) Also have a vanilla oak variant
    if key == 'jungle':
        for s in ['branch1', 'branch2', 'branch3', 'top']:
            tree('structure_templates/jungle_' + s, wood, s)
        tree('structure_templates/normal', wood, 'base')
        tree('structure_templates/normal_overlay', wood, 'overlay')

FRUIT_TREES = [
    'cacao',
    'date',
    'papaya',
    'almond',
    'cashew',
    'coconut',
    'hazelnut',
    'macadamia',
    'pistachio'
]

for tree in FRUIT_TREES:
    fruit_tree(tree)
