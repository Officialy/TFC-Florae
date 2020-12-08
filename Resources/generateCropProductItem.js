const fs = require('fs');

    let CROP_PRODUCT_TYPES = {
    'papyrus_pulp': 'papyrus_pulp',
    'papyrus_fiber': 'papyrus_fiber',
    'papyrus_paper': 'papyrus_paper',
    'agave': 'agave',
    'sisal_fiber': 'sisal_fiber',
    'sisal_string': 'sisal_string',
    'sisal_cloth': 'sisal_cloth',
    'cotton_boll': 'cotton_boll',
    'cotton_yarn': 'cotton_yarn',
    'cotton_cloth': 'cotton_cloth',
    'flax': 'flax',
    'flax_fiber': 'flax_fiber',
    'linen_string': 'linen_string',
    'linen_cloth': 'linen_cloth',
    'hemp': 'hemp',
    'hemp_fiber': 'hemp_fiber',
    'hemp_string': 'hemp_string',
    'hemp_cloth': 'hemp_cloth',
    'indigo': 'indigo',
    'madder': 'madder',
    'weld': 'weld',
    'woad': 'woad',
    'hops': 'hops',
    'rape': 'rape',
    'malt_barley': 'malt_barley',
    'malt_corn': 'malt_corn',
    'malt_rice': 'malt_rice',
    'malt_rye': 'malt_rye',
    'malt_wheat': 'malt_wheat',
    'malt_amaranth': 'malt_amaranth',
    'malt_buckwheat': 'malt_buckwheat',
    'malt_fonio': 'malt_fonio',
    'malt_millet': 'malt_millet',
    'malt_quinoa': 'malt_quinoa',
    'malt_spelt': 'malt_spelt',
    'malt_wild_rice': 'malt_wild_rice',
    'soybean_jute_disc': 'soybean_jute_disc',
    'soybean_silk_disc': 'soybean_silk_disc',
    'soybean_sisal_disc': 'soybean_sisal_disc',
    'soybean_cotton_disc': 'soybean_cotton_disc',
    'soybean_linen_disc': 'soybean_linen_disc',
    'soybean_papyrus_disc': 'soybean_papyrus_disc',
    'soybean_hemp_disc': 'soybean_hemp_disc',
    'linseed_jute_disc': 'linseed_jute_disc',
    'linseed_silk_disc': 'linseed_silk_disc',
    'linseed_sisal_disc': 'linseed_sisal_disc',
    'linseed_cotton_disc': 'linseed_cotton_disc',
    'linseed_linen_disc': 'linseed_linen_disc',
    'linseed_papyrus_disc': 'linseed_papyrus_disc',
    'linseed_hemp_disc': 'linseed_hemp_disc',
    'rape_seed_jute_disc': 'rape_seed_jute_disc',
    'rape_seed_silk_disc': 'rape_seed_silk_disc',
    'rape_seed_sisal_disc': 'rape_seed_sisal_disc',
    'rape_seed_cotton_disc': 'rape_seed_cotton_disc',
    'rape_seed_linen_disc': 'rape_seed_linen_disc',
    'rape_seed_papyrus_disc': 'rape_seed_papyrus_disc',
    'rape_seed_hemp_disc': 'rape_seed_hemp_disc',
    'sunflower_seed_jute_disc': 'sunflower_seed_jute_disc',
    'sunflower_seed_silk_disc': 'sunflower_seed_silk_disc',
    'sunflower_seed_sisal_disc': 'sunflower_seed_sisal_disc',
    'sunflower_seed_cotton_disc': 'sunflower_seed_cotton_disc',
    'sunflower_seed_linen_disc': 'sunflower_seed_linen_disc',
    'sunflower_seed_papyrus_disc': 'sunflower_seed_papyrus_disc',
    'sunflower_seed_hemp_disc': 'sunflower_seed_hemp_disc',
    'opium_poppy_seed_jute_disc': 'opium_poppy_seed_jute_disc',
    'opium_poppy_seed_silk_disc': 'opium_poppy_seed_silk_disc',
    'opium_poppy_seed_sisal_disc': 'opium_poppy_seed_sisal_disc',
    'opium_poppy_seed_cotton_disc': 'opium_poppy_seed_cotton_disc',
    'opium_poppy_seed_linen_disc': 'opium_poppy_seed_linen_disc',
    'opium_poppy_seed_papyrus_disc': 'opium_poppy_seed_papyrus_disc',
    'opium_poppy_seed_hemp_disc': 'opium_poppy_seed_hemp_disc',
    'sugar_beet_jute_disc': 'sugar_beet_jute_disc',
    'sugar_beet_silk_disc': 'sugar_beet_silk_disc',
    'sugar_beet_sisal_disc': 'sugar_beet_sisal_disc',
    'sugar_beet_cotton_disc': 'sugar_beet_cotton_disc',
    'sugar_beet_linen_disc': 'sugar_beet_linen_disc',
    'sugar_beet_papyrus_disc': 'sugar_beet_papyrus_disc',
    'sugar_beet_hemp_disc': 'sugar_beet_hemp_disc',
    'sugar_cane_jute_disc': 'sugar_cane_jute_disc',
    'sugar_cane_silk_disc': 'sugar_cane_silk_disc',
    'sugar_cane_sisal_disc': 'sugar_cane_sisal_disc',
    'sugar_cane_cotton_disc': 'sugar_cane_cotton_disc',
    'sugar_cane_linen_disc': 'sugar_cane_linen_disc',
    'sugar_cane_papyrus_disc': 'sugar_cane_papyrus_disc',
    'sugar_cane_hemp_disc': 'sugar_cane_hemp_disc',
    'olive_silk_disc': 'olive_silk_disc',
    'olive_sisal_disc': 'olive_sisal_disc',
    'olive_cotton_disc': 'olive_cotton_disc',
    'olive_linen_disc': 'olive_linen_disc',
    'olive_papyrus_disc': 'olive_papyrus_disc',
    'olive_hemp_disc': 'olive_hemp_disc',
    'silk_net': 'silk_net',
    'sisal_net': 'sisal_net',
    'cotton_net': 'cotton_net',
    'linen_net': 'linen_net',
    'papyrus_net': 'papyrus_net',
    'hemp_net': 'hemp_net',
    'dirty_silk_net': 'dirty_silk_net',
    'dirty_sisal_net': 'dirty_sisal_net',
    'dirty_cotton_net': 'dirty_cotton_net',
    'dirty_linen_net': 'dirty_linen_net',
    'dirty_papyrus_net': 'dirty_papyrus_net',
    'dirty_hemp_net': 'dirty_hemp_net',
    'silk_disc': 'silk_disc',
    'sisal_disc': 'sisal_disc',
    'cotton_disc': 'cotton_disc',
    'linen_disc': 'linen_disc',
    'papyrus_disc': 'papyrus_disc',
    'hemp_disc': 'hemp_disc',
    'chamomile_head': 'chamomile_head',
    'dandelion_head': 'dandelion_head',
    'labrador_tea_head': 'labrador_tea_head',
    'sunflower_head': 'sunflower_head'
    }

for(let cropProductType of Object.keys(CROP_PRODUCT_TYPES))
{
    generateRecipes(cropProductType)
}

function generateRecipes(cropProductType)
{
    let cropProductJSON = {
      "parent": "item/handheld",
      "textures": {
        "layer0": `tfcflorae:items/crop/product/${cropProductType}`
      }
    }
    fs.writeFileSync(`./src/main/resources/assets/tfcflorae/models/item/crop/product/${cropProductType}.json`, JSON.stringify(cropProductJSON, null, 2))
}