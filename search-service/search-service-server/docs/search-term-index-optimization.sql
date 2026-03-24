ALTER TABLE `search_term_index`
  ADD INDEX `idx_term_lookup` (`term_normalized`, `biz_type`, `term_type`, `weight`, `biz_id`),
  ADD INDEX `idx_biz_type_biz_id` (`biz_type`, `biz_id`);
