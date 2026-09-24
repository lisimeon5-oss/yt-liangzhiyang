// 重叠页更新已有商品，不再次追加同一秒杀记录。
export function mergeSeckillProducts(current, incoming) {
  const result = [];
  const positions = new Map();
  current.concat(incoming).forEach(item => {
    const key = String(item.id);
    if (positions.has(key)) {
      result[positions.get(key)] = item;
    } else {
      positions.set(key, result.length);
      result.push(item);
    }
  });
  return result;
}
