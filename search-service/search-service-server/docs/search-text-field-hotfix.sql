ALTER TABLE `search_term_index`
  ADD COLUMN `term_hash` CHAR(32) NOT NULL DEFAULT '' AFTER `term_normalized`;

UPDATE `search_term_index`
SET `term_hash` = MD5(`term_normalized`)
WHERE `term_hash` = '';

ALTER TABLE `search_term_index`
  DROP INDEX `uk_biz_term_type_field`,
  ADD UNIQUE INDEX `uk_biz_term_type_field` (`biz_type`, `biz_id`, `term_hash`, `term_type`, `source_field`),
  ADD INDEX `idx_term_hash_type_biz` (`term_hash`, `term_type`, `biz_type`),
  ADD INDEX `idx_term_hash_lookup` (`term_hash`, `biz_type`, `term_type`, `weight`, `biz_id`);
