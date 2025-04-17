import os
from typing import NamedTuple, Tuple, Dict, Set, List, Literal, Union
from collections import deque

from nbtlib import nbt, File as RootTag
from nbtlib.tag import String as StringTag, Int as IntTag, List as ListTag, Compound as CompoundTag


# Update paths to be consistent with generate_textures.py
current_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.abspath(os.path.join(current_dir, ".."))
test_trees_path = os.path.join(current_dir, 'test_trees')
output_structures_path = os.path.join(project_root, 'src/main/resources/data/tfc/structures/trees')

# Print paths for debugging
print(f"Using test trees path: {test_trees_path}")
print(f"Using output structures path: {output_structures_path}")

class Tree(NamedTuple):
    normal: str
    large: str | None
    dead: str | None


class Pos(NamedTuple):
    """ A `BlockPos` or basic 3D integer valued point. """

    x: int
    y: int
    z: int

    @staticmethod
    def from_nbt(pos_nbt: ListTag) -> 'Pos':
        return Pos(*map(int, pos_nbt))

    def norm1(self) -> int: return abs(self.x) + abs(self.y) + abs(self.z)

    def __add__(self, other) -> 'Pos': return Pos(self.x + other[0], self.y + other[1], self.z + other[2])
    def __sub__(self, other) -> 'Pos': return Pos(self.x - other[0], self.y - other[1], self.z - other[2])
    def __neg__(self) -> 'Pos': return Pos(-self.x, -self.y, -self.z)

    def __str__(self) -> str: return self.__repr__()
    def __repr__(self) -> str: return '(%d, %d, %d)' % self


class Worker(NamedTuple):
    size: Pos

    axis_states: Dict[int, Tuple[str, StringTag]]

    log_positions: Dict[Pos, int]
    leaf_positions: Dict[Pos, int]
    root_positions: Set[Pos]

    def get_trunk(self, dx: int = 0, dz: int = 0) -> Pos:
        return Pos(self.size.x // 2 + dx, 0, self.size.z // 2 + dz)

    def get_log(self, pos: Pos) -> Tuple[str, StringTag]:
        return self.axis_states[self.log_positions[pos]]

    def palette(self) -> 'Palette':
        return Palette([], [], self)

    def translate(self, offset: Pos) -> 'Worker':
        return Worker(self.size, self.axis_states, translate(self.log_positions, offset), translate(self.leaf_positions, offset), translate(self.root_positions, offset))


class Palette(NamedTuple):
    """ A palette representation of a structure. Stores a palette and positions in serialized NBT form. """

    blocks: List[CompoundTag]
    palette: List[CompoundTag]
    worker: Worker

    def add_blocks(self, wood: str, log_paths: Dict[Pos, Pos], leaf_paths: Dict[Pos, int]):
        """
        Adds all log and leaf blocks to the palette
        :param wood: The wood name, a TFC wood type
        :param log_paths: The log positions, mapped to a branch direction
        :param leaf_paths: The leaf positions, mapped to a distance
        """
        for log_pos, adj in log_paths.items():
            block_name, block_axis = self.worker.get_log(log_pos)
            block = create_log_block_tag(wood, block_name, block_axis, adj)

            self.add_block(log_pos, block)

        for leaf_pos, dist in leaf_paths.items():
            block = create_leaf_block_tag(wood, dist)

            self.add_block(leaf_pos, block)

    def add_block(self, pos: Pos, block: CompoundTag):
        if block in self.palette:
            block_id = self.palette.index(block)
        else:
            block_id = len(self.palette)
            self.palette.append(block)

        entry = CompoundTag()
        entry['state'] = IntTag(block_id)
        entry['pos'] = ListTag([IntTag(pos.x), IntTag(pos.y), IntTag(pos.z)])

        self.blocks.append(entry)

    def make_root(self) -> RootTag:
        """ Creates a new root tag representing the current structure. """
        return RootTag({
            'size': ListTag([IntTag(self.worker.size.x), IntTag(self.worker.size.y), IntTag(self.worker.size.z)]),
            'entities': ListTag(),
            'blocks': ListTag(self.blocks),
            'palette': ListTag(self.palette),
            'DataVersion': IntTag(DATA_VERSION),
        })


# Trunk directions are represented by a y = -2
TRUNK_NW: Pos = Pos(-1, -2, -1)
TRUNK_NE: Pos = Pos(1, -2, -1)
TRUNK_SW: Pos = Pos(-1, -2, 1)
TRUNK_SE: Pos = Pos(1, -2, 1)

# The default trunk direction used for the base of trees
TRUNK_ROOT: Pos = Pos(0, -1, 0)


# All possible branch directions
# Ordered by strongest connection.
BRANCH_DIRECTIONS: Dict[Pos, str] = {
    Pos(0, 0, 0): 'none',

    TRUNK_ROOT: 'down',

    Pos(0, 0, -1): 'north',
    Pos(-1, 0, 0): 'west',
    Pos(1, 0, 0): 'east',
    Pos(0, 0, 1): 'south',

    Pos(0, -1, -1): 'down_north',
    Pos(-1, -1, 0): 'down_west',
    Pos(1, -1, 0): 'down_east',
    Pos(0, -1, 1): 'down_south',

    Pos(-1, 0, -1): 'north_west',
    Pos(1, 0, -1): 'north_east',
    Pos(-1, 0, 1): 'south_west',
    Pos(1, 0, 1): 'south_east',

    Pos(-1, -1, -1): 'down_north_west',
    Pos(1, -1, -1): 'down_north_east',
    Pos(-1, -1, 1): 'down_south_west',
    Pos(1, -1, 1): 'down_south_east',

    Pos(-1, 1, -1): 'up_north_west',
    Pos(1, 1, -1): 'up_north_east',
    Pos(-1, 1, 1): 'up_south_west',
    Pos(1, 1, 1): 'up_south_east',

    Pos(0, 1, -1): 'up_north',
    Pos(1, 1, 0): 'up_west',
    Pos(-1, 1, 0): 'up_east',
    Pos(0, 1, 1): 'up_south',

    Pos(0, 1, 0): 'up',

    TRUNK_NW: 'trunk_north_west',
    TRUNK_NE: 'trunk_north_east',
    TRUNK_SW: 'trunk_south_west',
    TRUNK_SE: 'trunk_south_east',
}

# Exclude trunk directions here, but keep ordering
# This is the set of directions used for the BFS
NORMAL_BRANCH_DIRECTIONS: List[Pos] = [pos for pos in BRANCH_DIRECTIONS if pos.y != -2]

# Mapping of branch direction -> index in the list
BRANCH_STRENGTH: Dict[Pos, int] = {pos: i for i, pos in enumerate(NORMAL_BRANCH_DIRECTIONS)}

# Tuple of (dx, dy, trunk) where the dx, dy are offset from a divide-by-two center position
TRUNK_BRANCH_DIRECTIONS: Tuple[Tuple[int, int, Pos], ...] = (
    (0, 0, TRUNK_NW),
    (-1, 0, TRUNK_NE),
    (0, -1, TRUNK_SW),
    (-1, -1, TRUNK_SE),
)

LOG_BLOCKS = {'minecraft:oak_log', 'minecraft:oak_wood', 'minecraft:stripped_oak_log', 'minecraft:stripped_oak_wood'}
LEAF_BLOCKS = {'minecraft:oak_leaves'}

# The maximum allowed value of the 'distance' property across all leaves
MAX_DISTANCE = 9
DATA_VERSION = 2970

# Trees in TFC, along with their variants, and corresponding template files/names
TREES: Dict[str, Tree] = {
    'common_oak': Tree('common_oak', None, None)
}


def main():
    print('Making tree structures')
    for wood, tree in TREES.items():
        if tree.normal:
            make_tree(wood, tree.normal)
        if tree.large:
            make_tree(wood, tree.large, '_large')
        if tree.dead:
            make_tree(wood, tree.dead, '_dead')
    
    print('Done')


def make_tree(wood: str, tree: str | None, suffix: str = '') -> int:
    if tree is None:
        return 0
    
    tree_type, count = infer_type(tree)
    
    if isinstance(count, tuple):
        # Stacked
        total = 0
        for i, layer_count in enumerate(count, 1):
            for j in range(1, layer_count + 1):
                template = tree + '_layer%d_%d' % (i, j)
                worker = make_single_structure(wood, template)
                output_path = os.path.join(output_structures_path, f"{wood}{suffix}/layer{i}_{j}.nbt")
                save_structure(output_path, worker[0])
                total += 1
        return total
    elif count is None:
        # Overlay
        base_nbt, overlay_nbt, total = make_overlay_tree(wood, tree)
        base_path = os.path.join(output_structures_path, f"{wood}{suffix}/base.nbt")
        overlay_path = os.path.join(output_structures_path, f"{wood}{suffix}/overlay.nbt")
        save_structure(base_path, base_nbt)
        save_structure(overlay_path, overlay_nbt)
        return total
    else:
        # Random
        total = 0
        for i in range(1, count + 1):
            template = tree + str(i)
            worker = make_single_structure(wood, template)
            output_path = os.path.join(output_structures_path, f"{wood}{suffix}/{i}.nbt")
            save_structure(output_path, worker[0])
            total += 1
        return total


def infer_type(wood: str) -> Tuple[str, None | int | Tuple[int, ...]]:
    def exists(_path: str) -> bool:
        return os.path.exists(os.path.join(test_trees_path, f"{_path}.nbt"))
    
    # First, check if it's a stacked tree
    layers = []
    layer = 1
    while True:
        count = 0
        i = 1
        while exists('%s_layer%d_%d' % (wood, layer, i)):
            count += 1
            i += 1
        if count == 0:
            break
        layers.append(count)
        layer += 1
    
    if layers:
        return 'stacked', tuple(layers)
    
    # Next, check if it's an overlay
    if exists(wood) and exists(wood + '_overlay'):
        return 'overlay', None
    
    # Finally, check if it's a random one
    count = 0
    i = 1
    while exists('%s%d' % (wood, i)):
        count += 1
        i += 1
    
    if count > 0:
        return 'random', count
    
    raise ValueError("Could not infer tree type for '%s'" % wood)


def make_single_structure(wood: str, name: str) -> Tuple[RootTag, int]:
    input_path = os.path.join(test_trees_path, f"{name}.nbt")
    root_nbt = nbt.load(input_path)
    worker = make_worker(root_nbt)
    
    # Find the root position of the trunk (lowest y-coord)
    root_positions = sorted(worker.log_positions.keys(), key=lambda pos: (pos.y, pos.norm1()))
    if not root_positions:
        raise ValueError("Could not find any log blocks in structure '%s'" % name)
    
    # BFS from the root to find branch->branch connections
    log_paths = find_log_paths(root_positions, worker.log_positions)
    
    # BFS from all log blocks to find leaf distances
    leaf_paths = find_leaf_paths(worker.log_positions, worker.leaf_positions)
    
    # Create the new structure with remapped log and leaf blocks
    pal = worker.palette()
    pal.add_blocks(wood, log_paths, leaf_paths)
    
    return pal.make_root(), len(log_paths) + len(leaf_paths)


def make_overlay_tree(wood: str, tree: str) -> Tuple[RootTag, RootTag, int]:
    base_path = os.path.join(test_trees_path, f"{tree}.nbt")
    overlay_path = os.path.join(test_trees_path, f"{tree}_overlay.nbt")
    
    base_nbt = nbt.load(base_path)
    overlay_nbt = nbt.load(overlay_path)
    
    base_worker = make_worker(base_nbt)
    overlay_worker = make_worker(overlay_nbt)
    
    # Find the root position of the trunk (lowest y-coord)
    base_root_positions = sorted(base_worker.log_positions.keys(), key=lambda pos: (pos.y, pos.norm1()))
    overlay_root_positions = sorted(overlay_worker.log_positions.keys(), key=lambda pos: (pos.y, pos.norm1()))
    
    if not base_root_positions:
        raise ValueError("Could not find any log blocks in structure '%s'" % tree)
    
    # BFS from the root to find branch->branch connections
    base_log_paths = find_log_paths(base_root_positions, base_worker.log_positions)
    overlay_log_paths = find_log_paths(overlay_root_positions, overlay_worker.log_positions)
    
    # BFS from all log blocks to find leaf distances
    base_leaf_paths = find_leaf_paths(base_worker.log_positions, base_worker.leaf_positions)
    overlay_leaf_paths = find_leaf_paths(overlay_worker.log_positions, overlay_worker.leaf_positions)
    
    # Create the new structures with remapped log and leaf blocks
    base_pal = base_worker.palette()
    base_pal.add_blocks(wood, base_log_paths, base_leaf_paths)
    
    overlay_pal = overlay_worker.palette()
    overlay_pal.add_blocks(wood, overlay_log_paths, overlay_leaf_paths)
    
    return base_pal.make_root(), overlay_pal.make_root(), len(base_log_paths) + len(base_leaf_paths) + len(overlay_log_paths) + len(overlay_leaf_paths)


def make_worker(root_nbt: RootTag):
    size_nbt = root_nbt['size']
    size = Pos(size_nbt[0], size_nbt[1], size_nbt[2])
    
    palette_nbt = root_nbt['palette']
    blocks_nbt = root_nbt['blocks']
    
    log_states = states_of(palette_nbt, LOG_BLOCKS)
    leaf_states = states_of(palette_nbt, LEAF_BLOCKS)
    
    log_positions = positions_of(blocks_nbt, log_states)
    leaf_positions = positions_of(blocks_nbt, leaf_states)
    
    axis_states = {}
    for i, block in enumerate(palette_nbt):
        if i in log_states:
            name = block['Name'].value
            props = block.get('Properties', None)
            axis = None if props is None else props.get('axis', None)
            if axis is None:
                axis = StringTag('y')
            axis_states[i] = (name, axis)
    
    root_positions = set()
    for pos in log_positions.keys():
        if pos.y == 0:
            root_positions.add(pos)
    
    return Worker(size, axis_states, log_positions, leaf_positions, root_positions)


def states_of(palette_nbt: ListTag, blocks: Set[str]) -> Set[int]:
    states = set()
    for i, block in enumerate(palette_nbt):
        if block['Name'].value in blocks:
            states.add(i)
    return states


def positions_of(blocks_nbt: ListTag, states: Set[int]) -> Dict[Pos, int]:
    positions = {}
    for block in blocks_nbt:
        state = block['state'].value
        if state in states:
            positions[Pos.from_nbt(block['pos'])] = state
    return positions


def find_log_paths(root_positions: List[Pos], log_positions: Dict[Pos, int], enqueue: bool = True) -> Dict[Pos, Pos]:
    paths = {}
    queue = deque()
    visited = set()
    
    # All roots are visited, and enqueue their neighbors
    for root in root_positions:
        paths[root] = TRUNK_ROOT
        visited.add(root)
        
        if enqueue:
            for dir_pos in NORMAL_BRANCH_DIRECTIONS:
                if dir_pos == Pos(0, 0, 0):
                    continue
                neighbor = root + dir_pos
                if neighbor in log_positions and neighbor not in visited:
                    queue.append((neighbor, dir_pos))
    
    # The most 'strongest' connections are visited first (closest to the origin, by norm1)
    queue = deque(sorted(queue, key=lambda p: BRANCH_STRENGTH[p[1]]))
    
    # BFS search through log blocks, to find connections from branch -> branch
    while queue:
        pos, adj = queue.popleft()
        
        if pos in visited:
            continue
        
        paths[pos] = adj
        visited.add(pos)
        
        for dir_pos in NORMAL_BRANCH_DIRECTIONS:
            if dir_pos == Pos(0, 0, 0):
                continue
            neighbor = pos + dir_pos
            if neighbor in log_positions and neighbor not in visited:
                queue.append((neighbor, dir_pos))
    
    return paths


def find_leaf_paths(log_positions: Dict[Pos, int], leaf_positions: Dict[Pos, int]) -> Dict[Pos, int]:
    paths = {}
    queue = deque()
    visited = set()
    
    # Start with all leaves adjacent to logs, with a distance of 1
    for log_pos in log_positions.keys():
        visited.add(log_pos)
        for dir_pos in NORMAL_BRANCH_DIRECTIONS:
            if dir_pos == Pos(0, 0, 0):
                continue
            neighbor = log_pos + dir_pos
            if neighbor in leaf_positions and neighbor not in visited:
                queue.append((neighbor, 1))
                visited.add(neighbor)
                paths[neighbor] = 1
    
    # BFS search through leaves, increasing distance each time
    while queue:
        pos, dist = queue.popleft()
        
        if dist >= MAX_DISTANCE:
            continue
        
        for dir_pos in NORMAL_BRANCH_DIRECTIONS:
            if dir_pos == Pos(0, 0, 0):
                continue
            neighbor = pos + dir_pos
            if neighbor in leaf_positions and neighbor not in visited:
                queue.append((neighbor, dist + 1))
                visited.add(neighbor)
                paths[neighbor] = dist + 1
    
    return paths


def create_log_block_tag(wood: str, block_name: str, block_axis: StringTag, adj: Pos) -> CompoundTag:
    block = CompoundTag()
    block['Name'] = StringTag('tfcflorae:wood/wood/%s' % wood)
    
    props = CompoundTag()
    
    # Connections from a log to another block
    connection = BRANCH_DIRECTIONS[adj]
    if connection != 'none':
        props['branch'] = StringTag(connection)
    
    # Log axis
    props['axis'] = block_axis
    
    # Logs are natural
    props['natural'] = StringTag('true')
    
    block['Properties'] = props
    return block


def create_leaf_block_tag(wood: str, distance: int) -> CompoundTag:
    block = CompoundTag()
    block['Name'] = StringTag('tfcflorae:wood/leaves/%s' % wood)
    
    props = CompoundTag()
    
    # Leaves have a 'distance' property
    props['distance'] = StringTag(str(distance))
    
    # Leaves are persistent = false
    props['persistent'] = StringTag('false')
    
    block['Properties'] = props
    return block


def save_structure(path: str, root_nbt: RootTag):
    # Create output directory if it doesn't exist
    os.makedirs(os.path.dirname(path), exist_ok=True)
    
    # Save the structure file
    with open(path, 'wb') as f:
        root_nbt.write(f)


def translate(positions: Dict | Set, offset: Pos) -> Dict | Set:
    if isinstance(positions, dict):
        return {pos + offset: val for pos, val in positions.items()}
    else:
        return {pos + offset for pos in positions}


if __name__ == '__main__':
    main() 