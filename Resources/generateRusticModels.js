const fs = require('fs');

let WOOD_TYPES = {
    'african_padauk': 'african_padauk',
    'alder': 'alder',
    'angelim': 'angelim',
    'arrow_bamboo': 'arrow_bamboo',
    'baobab': 'baobab',
    'beech': 'beech',
    'black_walnut': 'black_walnut',
    'box': 'tall',
    'brazilwood': 'brazilwood',
    'butternut': 'butternut',
    'cinnamon': 'cinnamon',
    'cocobolo': 'cocobolo',
    'cypress': 'cypress',
    'ebony': 'ebony',
    'eucalyptus': 'eucalyptus',
    'european_oak': 'european_oak',
    'fever': 'fever',
    'fruitwood': 'fruitwood',
    'ginkgo': 'ginkgo',
    'greenheart': 'greenheart',
    'hawthorn': 'hawthorn',
    'hazel': 'hazel',
    'hemlock': 'hemlock',
    'holly': 'holly',
    'hornbeam': 'hornbeam',
    'ipe': 'ipe',
    'iroko': 'iroko',
    'ironwood': 'ironwood',
    'jacaranda': 'jacaranda',
    'juniper': 'juniper',
    'kauri': 'kauri',
    'larch': 'larch',
    'limba': 'limba',
    'locust': 'locust',
    'logwood': 'logwood',
    'maclura': 'maclura',
    'mahoe': 'mahoe',
    'mahogany': 'mahogany',
    'marblewood': 'marblewood',
    'messmate': 'messmate',
    'mountain_ash': 'mountain_ash',
    'nordmann_fir': 'nordmann_fir',
    'norway_spruce': 'norway_spruce',
    'pink_cherry': 'pink_cherry',
    'pink_ivory': 'pink_ivory',
    'poplar': 'poplar',
    'purpleheart': 'purpleheart',
    'red_cedar': 'red_cedar',
    'red_elm': 'red_elm',
    'redwood': 'redwood',
    'rowan': 'rowan',
    'rubber_fig': 'rubber_fig',
    'sweetgum': 'sweetgum',
    'syzygium': 'syzygium',
    'teak': 'teak',
    'wenge': 'wenge',
    'white_cherry': 'white_cherry',
    'white_elm': 'white_elm',
    'whitebeam': 'whitebeam',
    'yellow_meranti': 'yellow_meranti',
    'yew': 'yew',
    'zebrawood': 'zebrawood',
    'acacia': 'acacia',
    'ash': 'ash',
    'aspen': 'aspen',
    'birch': 'birch',
    'blackwood': 'blackwood',
    'chestnut': 'chestnut',
    'douglas_fir': 'douglas_fir',
    'hickory': 'hickory',
    'maple': 'maple',
    'oak': 'oak',
    'palm': 'palm',
    'pine': 'pine',
    'rosewood': 'rosewood',
    'sequoia': 'sequoia',
    'spruce': 'spruce',
    'sycamore': 'sycamore',
    'white_cedar': 'white_cedar',
    'willow': 'willow',
    'kapok': 'kapok',
    'hevea': 'hevea'
}


for(let woodType of Object.keys(WOOD_TYPES))
{
    generateRecipes(woodType)
}

function generateRecipes(woodType)
{
    let fence_gate_logJSON = {
	  "__comment": "Generated by generateResources.py function: blockstate",
	  "forge_marker": 1,
	  "defaults": {
		"model": "",
		"textures": {
		  "log": `tfc:blocks/wood/log/${woodType}`,
		  "planks": `tfc:blocks/wood/planks/${woodType}`,
		  "particle": `tfc:blocks/wood/log/${woodType}`
		},
		"uvlock": true
	  },
	  "variants": {
		"normal": [
		  {}
		],
		"facing=south,in_wall=false,open=false": {
		  "model": "tfcflorae:wood/fence_gate/fence_gate_closed"
		},
		"facing=west,in_wall=false,open=false": {
		  "model": "tfcflorae:wood/fence_gate/fence_gate_closed",
		  "y": 90
		},
		"facing=north,in_wall=false,open=false": {
		  "model": "tfcflorae:wood/fence_gate/fence_gate_closed",
		  "y": 180
		},
		"facing=east,in_wall=false,open=false": {
		  "model": "tfcflorae:wood/fence_gate/fence_gate_closed",
		  "y": 270
		},
		"facing=south,in_wall=false,open=true": {
		  "model": "tfcflorae:wood/fence_gate/fence_gate_open"
		},
		"facing=west,in_wall=false,open=true": {
		  "model": "tfcflorae:wood/fence_gate/fence_gate_open",
		  "y": 90
		},
		"facing=north,in_wall=false,open=true": {
		  "model": "tfcflorae:wood/fence_gate/fence_gate_open",
		  "y": 180
		},
		"facing=east,in_wall=false,open=true": {
		  "model": "tfcflorae:wood/fence_gate/fence_gate_open",
		  "y": 270
		},
		"facing=south,in_wall=true,open=false": {
		  "model": "tfcflorae:wood/fence_gate/wall_gate_closed"
		},
		"facing=west,in_wall=true,open=false": {
		  "model": "tfcflorae:wood/fence_gate/wall_gate_closed",
		  "y": 90
		},
		"facing=north,in_wall=true,open=false": {
		  "model": "tfcflorae:wood/fence_gate/wall_gate_closed",
		  "y": 180
		},
		"facing=east,in_wall=true,open=false": {
		  "model": "tfcflorae:wood/fence_gate/wall_gate_closed",
		  "y": 270
		},
		"facing=south,in_wall=true,open=true": {
		  "model": "tfcflorae:wood/fence_gate/wall_gate_open"
		},
		"facing=west,in_wall=true,open=true": {
		  "model": "tfcflorae:wood/fence_gate/wall_gate_open",
		  "y": 90
		},
		"facing=north,in_wall=true,open=true": {
		  "model": "tfcflorae:wood/fence_gate/wall_gate_open",
		  "y": 180
		},
		"facing=east,in_wall=true,open=true": {
		  "model": "tfcflorae:wood/fence_gate/wall_gate_open",
		  "y": 270
		}
	  }
	}
    let fence_gate_log_itemJSON = {
	  "__comment": "Generated by generateResources.py function: model",
	  "parent": "tfcflorae:block/wood/fence_gate/fence_gate_closed",
	  "textures": {
		"log": `tfc:blocks/wood/log/${woodType}`,
		"planks": `tfc:blocks/wood/planks/${woodType}`,
		"particle": `tfc:blocks/wood/log/${woodType}`
	  }
	}
    let fence_gate_log_recipeJSON = {
        "type": "minecraft:crafting_shaped",
        "pattern": [
          "XBX",
          "XBX"
        ],
        "key": {
          "X": {
            "item": `tfc:wood/lumber/${woodType}`
          },
          "B": {
            "item": `tfc:wood/log/${woodType}`
          }
        },
        "result": {
          "item": `tfcflorae:wood/fence_gate_log/${woodType}`,
          "count": 8
        }
    }
    fs.writeFileSync(`./src/main/resources/assets/tfcflorae/blockstates/wood/fence_gate_log/${woodType}.json`, JSON.stringify(fence_gate_logJSON, null, 2))
    fs.writeFileSync(`./src/main/resources/assets/tfcflorae/models/item/wood/fence_gate_log/${woodType}.json`, JSON.stringify(fence_gate_log_itemJSON, null, 2))
    fs.writeFileSync(`./src/main/resources/assets/tfcflorae/recipes/wood/fence_gate_log/${woodType}.json`, JSON.stringify(fence_gate_log_recipeJSON, null, 2))
}