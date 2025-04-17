from PIL import Image, ImageDraw, ImageEnhance, ImageOps
from PIL.Image import Transpose

import colorsys
from constants import *
import os
import sys

# Update paths to point to correct locations
# Assuming resources directory is the current working directory
current_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.abspath(os.path.join(current_dir, ".."))
path = os.path.join(project_root, 'src/main/resources/assets/tfc/textures/')
mc_path = os.path.join(project_root, 'src/main/resources/assets/minecraft/textures/')
templates = os.path.join(current_dir, 'texture_templates/')

# Print paths for debugging
print(f"Current directory: {current_dir}")
print(f"Using texture path: {path}")
print(f"Using template path: {templates}")

# Function to ensure a directory exists
def ensure_dir(dir_path):
    if not os.path.exists(dir_path):
        print(f"Creating directory: {dir_path}")
        os.makedirs(dir_path, exist_ok=True)

# Function to check if a file exists and print error if it doesn't
def check_file_exists(file_path):
    if not os.path.exists(file_path):
        print(f"ERROR: File does not exist: {file_path}")
        return False
    return True

def overlay_image(front_file_dir, back_file_dir, result_dir, mask: str = None):
    front_path = front_file_dir + '.png'
    back_path = back_file_dir + '.png'
    
    if not check_file_exists(front_path) or not check_file_exists(back_path):
        return
        
    # Ensure the output directory exists
    output_dir = os.path.dirname(result_dir)
    ensure_dir(output_dir)
    
    foreground = Image.open(front_path).convert('RGBA')
    background = Image.open(back_path).convert('RGBA')
    
    if mask is None:
        mask = foreground
    else:
        mask_path = mask + '.png'
        if not check_file_exists(mask_path):
            return
        mask = Image.open(mask_path).convert('L')
        
    background.paste(foreground, (0, 0), mask)
    background.save(result_dir + '.png')

def create_chest(wood: str):
    log_path = path + 'block/wood/log/%s.png' % wood
    sheet_path = path + 'block/wood/sheet/%s.png' % wood
    
    # Check if the source files exist
    if not check_file_exists(log_path) or not check_file_exists(sheet_path):
        print(f"Skipping chest creation for {wood} due to missing source files")
        return
    
    # Ensure output directories exist
    ensure_dir(os.path.dirname(path + 'entity/chest/normal/%s.png' % wood))
    ensure_dir(os.path.dirname(path + 'entity/chest/trapped/%s.png' % wood))
    ensure_dir(os.path.dirname(path + 'entity/chest/normal_left/%s.png' % wood))
    ensure_dir(os.path.dirname(path + 'entity/chest/normal_right/%s.png' % wood))
    ensure_dir(os.path.dirname(path + 'entity/chest/trapped_left/%s.png' % wood))
    ensure_dir(os.path.dirname(path + 'entity/chest/trapped_right/%s.png' % wood))
    
    log = Image.open(log_path).convert('RGBA').crop((0, 0, 14, 14))
    sheet = Image.open(sheet_path).convert('RGBA').crop((0, 0, 14, 14))
    empty = (0, 0, 0, 0)
    frame = log.copy()
    ImageDraw.Draw(frame).rectangle((1, 1, 12, 12), fill=empty)
    top = sheet.copy().transpose(Transpose.TRANSVERSE)
    top.paste(frame, (0, 0), frame)

    side = top.copy()
    ImageDraw.Draw(side).rectangle((0, 0, 14, 3), fill=empty)
    log_section = log.copy()
    ImageDraw.Draw(log_section).rectangle((0, 1, 14, 14), fill=empty)
    side.paste(log_section, (0, 4), log_section)
    side.paste(log_section, (0, 13), log_section)

    rim = top.copy()
    ImageDraw.Draw(rim).rectangle((0, 0, 14, 9), fill=empty)
    rim.paste(log_section, (0, 9), log_section)
    underside = top.copy()
    ImageDraw.Draw(underside).rectangle((2, 2, 11, 11), fill=(0, 0, 0, 255))

    cover = top.copy()
    shaded_square = Image.new('RGBA', (10, 10), (0, 0, 0, 180))
    blank = Image.new('RGBA', (14, 14), empty)
    blank.paste(shaded_square, (2, 2), shaded_square)
    cover = Image.alpha_composite(cover, blank)

    handle = Image.open(templates + 'chest/handle.png').convert('RGBA')
    normal = Image.new('RGBA', (64, 64), empty)
    normal.paste(handle, (0, 0), handle)
    normal.paste(cover, (14, 0), cover)
    normal.paste(top, (28, 0), top)
    for i in range(0, 4):
        normal.paste(rim, (i * 14, 5), rim)
        normal.paste(side, (i * 14, 29), side)
    normal.paste(top, (14, 19), top)
    normal.paste(underside, (28, 19), underside)
    normal.save(path + 'entity/chest/normal/%s' % wood + '.png')
    trapped = normal.copy()
    trapped_overlay = Image.open(templates + 'chest/trapped_overlay.png')
    trapped = Image.alpha_composite(trapped, trapped_overlay)
    trapped.save(path + 'entity/chest/trapped/%s' % wood + '.png')

    # Double Chests
    log_rect = Image.open(path + 'block/wood/log/%s' % wood + '.png').convert('RGBA').crop((0, 0, 15, 14))
    sheet_rect = Image.open(path + 'block/wood/sheet/%s' % wood + '.png').convert('RGBA').crop((0, 0, 15, 14))

    top_right = sheet_rect.copy()
    top_right_frame = log_rect.copy()
    ImageDraw.Draw(top_right_frame).rectangle((0, 1, 13, 12), fill=empty)
    top_right.paste(top_right_frame, (0, 0), top_right_frame)

    top_left = sheet_rect.copy()
    top_left_frame = log_rect.copy()
    ImageDraw.Draw(top_left_frame).rectangle((1, 1, 15, 12), fill=empty)
    top_left.paste(top_left_frame, (0, 0), top_left_frame)

    underside_right = top_right.copy()
    ImageDraw.Draw(underside_right).rectangle((0, 2, 12, 11), fill=(0, 0, 0, 255))
    underside_left = top_left.copy()
    ImageDraw.Draw(underside_left).rectangle((2, 2, 15, 11), fill=(0, 0, 0, 255))

    cover_right = top_right.copy()
    shaded_rectangle = Image.new('RGBA', (13, 10), (0, 0, 0, 180))
    blank = Image.new('RGBA', (15, 14), empty)
    blank.paste(shaded_rectangle, (0, 2), shaded_rectangle)
    cover_right = Image.alpha_composite(cover_right, blank)
    cover_left = top_left.copy()
    blank = Image.new('RGBA', (15, 14), empty)
    blank.paste(shaded_rectangle, (2, 2), shaded_rectangle)
    cover_left = Image.alpha_composite(cover_left, blank)

    rim_right = top_right.copy()
    ImageDraw.Draw(rim_right).rectangle((0, 0, 15, 9), fill=empty)
    rim_right.paste(log_section, (0, 9), log_section)
    rim_right.paste(log_section, (1, 9), log_section)
    rim_left = top_left.copy()
    ImageDraw.Draw(rim_left).rectangle((0, 0, 15, 9), fill=empty)
    rim_left.paste(log_section, (0, 9), log_section)
    rim_left.paste(log_section, (1, 9), log_section)

    side_right = top_right.copy()
    ImageDraw.Draw(side_right).rectangle((0, 0, 15, 3), fill=empty)
    log_section = log.copy()
    ImageDraw.Draw(log_section).rectangle((0, 1, 15, 14), fill=empty)
    side_right.paste(log_section, (0, 4), log_section)
    side_right.paste(log_section, (1, 4), log_section)
    side_right.paste(log_section, (0, 13), log_section)
    side_right.paste(log_section, (1, 13), log_section)
    side_left = top_left.copy()
    ImageDraw.Draw(side_left).rectangle((0, 0, 15, 3), fill=empty)
    log_section = log.copy()
    ImageDraw.Draw(log_section).rectangle((0, 1, 15, 14), fill=empty)
    side_left.paste(log_section, (0, 4), log_section)
    side_left.paste(log_section, (1, 4), log_section)
    side_left.paste(log_section, (0, 13), log_section)
    side_left.paste(log_section, (1, 13), log_section)

    normal_left = Image.new('RGBA', (64, 64), empty)
    handle = Image.open(templates + 'chest/handle_left.png')
    normal_left.paste(handle, (0, 0), handle)
    normal_left.paste(cover_right, (14, 0), cover_right)
    normal_left.paste(top_right, (29, 0), top_right)
    normal_left.paste(rim_right, (14, 5), rim_right)
    normal_left.paste(rim, (29, 5), rim)
    normal_left.paste(rim_left, (43, 5), rim_left)
    normal_left.paste(top_right, (14, 19), top_right)
    normal_left.paste(underside_right, (29, 19), underside_right)
    normal_left.paste(side, (29, 29), side)
    normal_left.paste(side_right, (14, 29), side_right)
    normal_left.paste(side_left, (43, 29), side_left)
    normal_left.save(path + 'entity/chest/normal_left/%s' % wood + '.png')
    left_trapped_overlay = Image.open(templates + 'chest/trapped_left_overlay.png')
    left_trapped = Image.alpha_composite(normal_left, left_trapped_overlay)
    left_trapped.save(path + 'entity/chest/trapped_left/%s' % wood + '.png')

    normal_right = Image.new('RGBA', (64, 64), empty)
    handle = Image.open(templates + 'chest/handle_right.png')
    normal_right.paste(handle, (0, 0), handle)
    normal_right.paste(cover_left, (14, 0), cover_left)
    normal_right.paste(top_left, (29, 0), top_left)
    normal_right.paste(rim, (0, 5), rim)
    normal_right.paste(rim_left, (14, 5), rim_left)
    normal_right.paste(rim_right, (43, 5), rim_right)
    normal_right.paste(top_left, (14, 19), top_left)
    normal_right.paste(cover_left, (29, 19), cover_left)
    normal_right.paste(underside_left, (29, 19), underside_left)
    normal_right.paste(side, (0, 29), side)
    normal_right.paste(side_left, (14, 29), side_right)
    normal_right.paste(side_right, (43, 29), side_left)
    normal_right.save(path + 'entity/chest/normal_right/%s' % wood + '.png')
    right_trapped_overlay = Image.open(templates + 'chest/trapped_right_overlay.png')
    right_trapped = Image.alpha_composite(normal_right, right_trapped_overlay)
    right_trapped.save(path + 'entity/chest/trapped_right/%s' % wood + '.png')

def create_chest_boat(wood: str):
    log = Image.open(path + 'block/wood/log/%s.png' % wood).convert('RGBA')
    sheet = Image.open(path + 'block/wood/sheet/%s.png' % wood).convert('RGBA').transpose(Transpose.TRANSVERSE)
    log_mask = Image.open(templates + 'chest_boat_log_mask.png').convert('L')
    sheet_mask = Image.open(templates + 'chest_boat_sheet_mask.png').convert('L')
    big_log = fill_image(log, 128, 128, 16, 16)
    big_sheet = fill_image(sheet, 128, 128, 16, 16)
    cover = Image.open(templates + 'chest_boat_static.png')

    base = Image.new('RGBA', (128, 128))
    base.paste(big_log, mask=log_mask)
    base.paste(big_sheet, mask=sheet_mask)
    base.paste(cover, mask=cover)
    base.save(path + 'entity/chest_boat/%s.png' % wood)

def create_hanging_sign(wood: str, metal: str):
    img = Image.new('RGBA', (64, 32))
    sheet = Image.open(path + 'block/wood/sheet/%s.png' % wood).convert('RGBA').transpose(Transpose.TRANSVERSE)
    big_sheet = fill_image(sheet, 64, 32, 16, 16)
    mask = Image.open(templates + 'hanging_sign.png').convert('L')
    img.paste(big_sheet, mask=mask)
    smooth = Image.open(path + 'block/metal/smooth/%s.png' % metal).convert('RGBA').transpose(Transpose.TRANSVERSE)
    big_smooth = fill_image(smooth, 64, 32, 16, 16)
    chain_mask = Image.open(templates + 'hanging_sign_chains.png').convert('L')
    img.paste(big_smooth, mask=chain_mask)
    img.save(path + 'entity/signs/hanging/%s/%s.png' % (metal, wood))

    img = Image.new('RGBA', (16, 16))
    img.paste(sheet, mask=Image.open(templates + 'hanging_sign_edit.png').convert('L'))
    img.paste(smooth, mask=Image.open(templates + 'hanging_sign_edit_overlay.png').convert('L'))
    img.save(path + 'gui/hanging_signs/%s/%s.png' % (metal, wood))

def fill_image(tile_instance, width: int, height: int, tile_width: int, tile_height: int):
    image_instance = Image.new('RGBA', (width, height))
    for i in range(0, int(width / tile_width)):
        for j in range(0, int(height / tile_height)):
            image_instance.paste(tile_instance, (i * tile_width, j * tile_height))
    return image_instance

def stitch_images(width: int, height: int, name: str, paths: List[str]):
    img = Image.new('RGBA', (width, height))
    images = []
    for fp in paths:
        images.append(Image.open(fp + '.png').convert('RGBA'))
    for i in range(0, width, 16):
        for j in range(0, height, 16):
            k = (i // 16) + (j // 16) * (width // 16)
            if k < len(images):
                img.paste(images[k], (i, j))
    img.save(name + '.png')

def create_bookshelf(wood: str):
    side = Image.open(path + 'block/wood/planks/%s.png' % wood).convert('RGBA')
    overlay = Image.open(templates + 'bookshelf_side.png').convert('RGBA')
    side.paste(overlay, (0, 0), overlay)
    side.save(path + 'block/wood/bookshelf/%s.png' % wood)

def create_sign(wood: str):
    sheet = Image.open(path + 'block/wood/sheet/%s.png' % wood).convert('RGBA')
    mask = Image.open(templates + 'sign_mask.png').convert('L')
    overlay = Image.open(templates + 'sign_overlay.png').convert('RGBA')
    img = Image.new('RGBA', (16, 16))
    img.paste(sheet, mask=mask)
    img.paste(overlay, mask=overlay)
    img.save(path + 'block/wood/sign/%s.png' % wood)

def create_sign_item(wood: str, log_color):
    easy_colorize(log_color, templates + 'sign_item', path + 'item/wood/sign/%s' % wood)

def create_hanging_sign_chains_item(metal: str, smooth_color):
    easy_colorize(smooth_color, templates + 'hanging_sign_chains_item', path + 'item/metal/hanging_sign_chains/%s' % metal)

def create_magma(rock: str):
    easy_colorize((50, 17, 10), templates + 'magma', path + 'block/rock/magma/%s' % rock)

def create_horse_chest(wood: str, plank_color, log_color):
    side_base = Image.open(templates + 'chest/horse_chest.png').convert('RGBA')
    side = put_on_all_pixels(side_base, plank_color)
    log_mask = Image.open(templates + 'chest/horse_chest_log_mask.png').convert('L')
    log_overlay = Image.new('RGBA', side.size, (0, 0, 0, 0))
    log_parts = Image.new('RGBA', side.size, log_color)
    log_overlay.paste(log_parts, mask=log_mask)
    side = Image.alpha_composite(side, log_overlay)
    black_parts = Image.open(templates + 'chest/horse_chest_black.png').convert('RGBA')
    side = Image.alpha_composite(side, black_parts)
    side.save(path + 'entity/chest/horse/%s.png' % wood)

def get_wood_colors(wood_path: str):
    wood = Image.open(path + 'block/wood/%s.png' % wood_path)
    return wood.getpixel((0, 0))

def get_metal_colors(metal_path: str):
    metal = Image.open(path + 'block/metal/%s.png' % metal_path)
    return metal.getpixel((0, 0))

def easy_colorize(color, from_path, to_path, saturation: float = 1):
    img = Image.open(from_path + '.png')
    new_image = put_on_all_pixels(img, color)
    if saturation != 1:
        new_image = ImageEnhance.Color(new_image).enhance(saturation)
    new_image.save(to_path + '.png')

def put_on_all_pixels(img: Image, color, dark_threshold: int = 50) -> Image:
    if isinstance(color, int):
        color = (color, color, color, 255)
    img = img.convert('RGBA')
    for x in range(0, img.width):
        for y in range(0, img.height):
            dat = img.getpixel((x, y))
            grey = (dat[0] + dat[1] + dat[2]) / 3 / 255
            if dat[3] > 0:
                tup = (int(color[0] * grey), int(color[1] * grey), int(color[2] * grey), dat[3])
                img.putpixel((x, y), tup)
    return img

def create_boat_texture(wood: str):
    plank = Image.open(path + 'block/wood/planks/%s.png' % wood).convert('RGBA')
    overlay = Image.open(templates + 'boat.png').convert('RGBA')
    img = manual_palette_swap(overlay, Image.open(templates + 'oak_boat_key.png'), plank)
    img.save(path + 'entity/boat/%s.png' % wood)
    img.save(path + 'item/wood/boat/%s.png' % wood)
    
    overlay = Image.open(templates + 'boat_with_chest.png').convert('RGBA')
    img = manual_palette_swap(overlay, Image.open(templates + 'oak_boat_key.png'), plank)
    img.save(path + 'entity/chest_boat/%s_chest.png' % wood)
    img.save(path + 'item/wood/chest_boat/%s.png' % wood)

def manual_palette_swap(img: Image, palette_key: Image, palette: Image) -> Image:
    data = {}
    for x in range(palette_key.width):
        for y in range(palette_key.height):
            src = palette_key.getpixel((x, y))
            dst = palette.getpixel((x % palette.width, y % palette.height))
            if src[3] == 0 or dst[3] == 0:
                continue
            data[src] = dst
    img = img.copy()
    for x in range(0, img.width):
        for y in range(0, img.height):
            dat = img.getpixel((x, y))
            if dat in data:
                img.putpixel((x, y), data[dat])
    return img

def main():
    # Set up a basic wood texture for testing if needed
    test_wood_dir = os.path.join(path, 'block/wood')
    ensure_dir(os.path.join(test_wood_dir, 'log'))
    ensure_dir(os.path.join(test_wood_dir, 'sheet'))
    
    # If no wood textures exist, create a simple test one
    test_wood = 'test_wood'
    test_log_path = os.path.join(test_wood_dir, 'log', f'{test_wood}.png')
    test_sheet_path = os.path.join(test_wood_dir, 'sheet', f'{test_wood}.png')
    
    if not os.path.exists(test_log_path):
        print(f"Creating test wood texture at {test_log_path}")
        img = Image.new('RGBA', (16, 16), (139, 69, 19, 255))  # Brown color
        img.save(test_log_path)
    
    if not os.path.exists(test_sheet_path):
        print(f"Creating test wood sheet texture at {test_sheet_path}")
        img = Image.new('RGBA', (16, 16), (160, 82, 45, 255))  # Slightly different brown
        img.save(test_sheet_path)
    
    # Try with our test wood first
    print(f"Creating chest for test wood: {test_wood}")
    try:
        create_chest(test_wood)
    except Exception as e:
        print(f"Error creating test chest: {e}")
    
    # Now try the regular woods if they exist
    for wood in WOODS.keys():
        try:
            print(f"Attempting to create chest for {wood}")
            create_chest(wood)
        except Exception as e:
            print(f"Error creating chest for {wood}: {e}")

    print('Done')


if __name__ == '__main__':
    main()
