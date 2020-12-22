const fs = require('fs');

let STONE_TYPES = {
    'andesite': 'andesite',
    'arkose': 'arkose',
    'basalt': 'basalt',
    'blaimorite': 'blaimorite',
    'blueschist': 'blueschist',
    'boninite': 'boninite',
    'carbonatite': 'carbonatite',
    'cataclasite': 'cataclasite',
    'chalk': 'chalk',
    'chert': 'chert',
    'claystone': 'claystone',
    'conglomerate': 'conglomerate',
    'dacite': 'dacite',
    'diorite': 'diorite',
    'dolomite': 'dolomite',
    'foidolite': 'foidolite',
    'gabbro': 'gabbro',
    'gneiss': 'gneiss',
    'granite': 'granite',
    'greenschist': 'greenschist',
    'jaspillite': 'jaspillite',
    'limestone': 'limestone',
    'marble': 'marble',
    'mylonite': 'mylonite',
    'phyllite': 'phyllite',
    'quartzite': 'quartzite',
    'rhyolite': 'rhyolite',
    'rocksalt': 'rocksalt',
    'schist': 'schist',
    'shale': 'shale',
    'slate': 'slate',
    'travertine': 'travertine',
    'wackestone': 'wackestone',
	  'breccia': 'breccia',
    'porphyry': 'porphyry',
    'peridotite': 'peridotite',
    'mudstone': 'mudstone',
    'sandstone': 'sandstone',
    'siltstone': 'siltstone',
    'catlinite': 'catlinite',
    'novaculite': 'novaculite',
    'soapstone': 'soapstone',
    'komatiite': 'komatiite'
}


for(let stoneType of Object.keys(STONE_TYPES))
{
    generateRecipes(stoneType)
}

function generateRecipes(stoneType)
{
      let mossy_raw_layerJSON = {
        "__comment": "Generated by generateResources.py function: blockstate",
        "forge_marker": 1,
        "defaults": {
          "model": "cube_all",
          "textures": {
            "all": `tfc:blocks/stonetypes/raw/${stoneType}`,
			      "particle": `tfc:blocks/stonetypes/raw/${stoneType}`,
            "moss": "tfcflorae:blocks/stonetypes/moss_overlay"
          }
        },
        "variants": {
          "normal": [
            {}
          ]
        }
      }
      let mossy_rawJSON = {
        "__comment": "Generated by generateResources.py function: blockstate",
        "forge_marker": 1,
        "defaults": {
          "model": "cube_all",
          "textures": {
            "all": `tfcflorae:blocks/stonetypes/mossy_raw/${stoneType}`
          }
        },
        "variants": {
          "normal": [
            {}
          ]
        }
      }
      let mudJSON = {
        "__comment": "Generated by generateResources.py function: blockstate",
        "forge_marker": 1,
        "defaults": {
          "model": "cube_all",
          "textures": {
            "all": `tfcflorae:blocks/stonetypes/mud/${stoneType}`
          }
        },
        "variants": {
          "normal": [
            {}
          ]
        }
      }
      let mud_brickJSON = {
        "__comment": "Generated by generateResources.py function: blockstate",
        "forge_marker": 1,
        "defaults": {
          "model": "cube_all",
          "textures": {
            "all": `tfcflorae:blocks/stonetypes/mud_brick/${stoneType}`
          }
        },
        "variants": {
          "normal": [
            {}
          ]
        }
      }
      let coarse_dirtJSON = {
	  "__comment": "Generated by generateResources.py function: blockstate",
	  "forge_marker": 1,
	  "defaults": {
		"model": "cube_all",
		"textures": {
		  "all": `tfcflorae:blocks/stonetypes/coarse_dirt/${stoneType}`
		}
	  },
	  "variants": {
		"normal": [
		  {}
		]
	  }
	}
      let podzolJSON = {
	  "__comment": "Generated by generateResources.py function: blockstate",
	  "forge_marker": 1,
	  "defaults": {
		"model": "tfc:grass",
		"textures": {
		  "all": `tfc:blocks/stonetypes/dirt/${stoneType}`,
		  "particle": "tfcflorae:blocks/podzol_top",
		  "top": "tfcflorae:blocks/podzol_top",
		  "north": "tfcflorae:blocks/podzol_side",
		  "south": "tfcflorae:blocks/podzol_side",
		  "east": "tfcflorae:blocks/podzol_side",
		  "west": "tfcflorae:blocks/podzol_side"
		}
	  },
	  "variants": {
		"normal": [
		  {}
		],
		"north": {
		  "true": {
			"textures": {
			  "north": "tfcflorae:blocks/podzol_top"
			}
		  },
		  "false": {}
		},
		"south": {
		  "true": {
			"textures": {
			  "south": "tfcflorae:blocks/podzol_top"
			}
		  },
		  "false": {}
		},
		"east": {
		  "true": {
			"textures": {
			  "east": "tfcflorae:blocks/podzol_top"
			}
		  },
		  "false": {}
		},
		"west": {
		  "true": {
			"textures": {
			  "west": "tfcflorae:blocks/podzol_top"
			}
		  },
		  "false": {}
		}
	  }
	}
      let coarse_dirt_recipeJSON = {
	  "type": "minecraft:crafting_shaped",
	  "pattern": [
		"BX",
		"XB"
	  ],
	  "key": {
		"X": {
		  "type": "forge:ore_dict",
		  "ore": `gravel${stoneType}`
	    },
        "B": {
		  "type": "forge:ore_dict",
		  "ore": `dirt${stoneType}`
        }
	  },
	  "result": {
		"item": `tfcflorae:coarse_dirt/${stoneType}`,
		"count": 4
	  }
	}
    fs.writeFileSync(`./src/main/resources/assets/tfcflorae/blockstates/mossy_raw_layer/${stoneType}.json`, JSON.stringify(mossy_raw_layerJSON, null, 2))
    fs.writeFileSync(`./src/main/resources/assets/tfcflorae/blockstates/mossy_raw/${stoneType}.json`, JSON.stringify(mossy_rawJSON, null, 2))
    fs.writeFileSync(`./src/main/resources/assets/tfcflorae/blockstates/mud/${stoneType}.json`, JSON.stringify(mudJSON, null, 2))
    fs.writeFileSync(`./src/main/resources/assets/tfcflorae/blockstates/mud_brick/${stoneType}.json`, JSON.stringify(mud_brickJSON, null, 2))
    fs.writeFileSync(`./src/main/resources/assets/tfcflorae/blockstates/coarse_dirt/${stoneType}.json`, JSON.stringify(coarse_dirtJSON, null, 2))
    fs.writeFileSync(`./src/main/resources/assets/tfcflorae/blockstates/podzol/${stoneType}.json`, JSON.stringify(podzolJSON, null, 2))
    fs.writeFileSync(`./src/main/resources/assets/tfcflorae/recipes/stone/coarse_dirt/${stoneType}.json`, JSON.stringify(coarse_dirt_recipeJSON, null, 2))
}